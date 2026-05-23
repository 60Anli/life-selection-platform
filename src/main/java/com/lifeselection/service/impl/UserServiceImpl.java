package com.lifeselection.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lifeselection.dto.LoginFormDTO;
import com.lifeselection.dto.Result;
import com.lifeselection.dto.UserDTO;
import com.lifeselection.entity.User;
import com.lifeselection.mapper.UserMapper;
import com.lifeselection.service.IUserService;
import com.lifeselection.utils.RegexUtils;
import com.lifeselection.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.lifeselection.utils.RedisConstants.*;
import static com.lifeselection.utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * <p>
 * 鏈嶅姟瀹炵幇绫?
 * </p>
 *
 * @author 铏庡摜
 * @since 2021-12-22
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result sendCode(String phone, HttpSession session) {
        // 1.鏍￠獙鎵嬫満鍙?
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 2.濡傛灉涓嶇鍚堬紝杩斿洖閿欒淇℃伅
            return Result.fail("鎵嬫満鍙锋牸寮忛敊璇紒");
        }
        // 3.绗﹀悎锛岀敓鎴愰獙璇佺爜
        String code = RandomUtil.randomNumbers(6);

        // 4.淇濆瓨楠岃瘉鐮佸埌 session
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);

        // 5.鍙戦€侀獙璇佺爜
        log.debug("鍙戦€佺煭淇￠獙璇佺爜鎴愬姛锛岄獙璇佺爜锛歿}", code);
        // 杩斿洖ok
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        // 1.鏍￠獙鎵嬫満鍙?
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            // 2.濡傛灉涓嶇鍚堬紝杩斿洖閿欒淇℃伅
            return Result.fail("鎵嬫満鍙锋牸寮忛敊璇紒");
        }
        // 3.浠巖edis鑾峰彇楠岃瘉鐮佸苟鏍￠獙
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        String code = loginForm.getCode();
        if (cacheCode == null || !cacheCode.equals(code)) {
            // 涓嶄竴鑷达紝鎶ラ敊
            return Result.fail("验证码错误");
        }

        // 4.涓€鑷达紝鏍规嵁鎵嬫満鍙锋煡璇㈢敤鎴?select * from tb_user where phone = ?
        User user = query().eq("phone", phone).one();

        // 5.鍒ゆ柇鐢ㄦ埛鏄惁瀛樺湪
        if (user == null) {
            // 6.涓嶅瓨鍦紝鍒涘缓鏂扮敤鎴峰苟淇濆瓨
            user = createUserWithPhone(phone);
        }

        // 7.淇濆瓨鐢ㄦ埛淇℃伅鍒?redis涓?
        // 7.1.闅忔満鐢熸垚token锛屼綔涓虹櫥褰曚护鐗?
        String token = UUID.randomUUID().toString(true);
        // 7.2.灏哢ser瀵硅薄杞负HashMap瀛樺偍
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName, fieldValue) -> fieldValue.toString()));
        // 7.3.瀛樺偍
        String tokenKey = LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey, userMap);
        // 7.4.璁剧疆token鏈夋晥鏈?
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);

        // 8.杩斿洖token
        return Result.ok(token);
    }

    @Override
    public Result sign() {
        // 1.鑾峰彇褰撳墠鐧诲綍鐢ㄦ埛
        Long userId = UserHolder.getUser().getId();
        // 2.鑾峰彇鏃ユ湡
        LocalDateTime now = LocalDateTime.now();
        // 3.鎷兼帴key
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = USER_SIGN_KEY + userId + keySuffix;
        // 4.鑾峰彇浠婂ぉ鏄湰鏈堢殑绗嚑澶?
        int dayOfMonth = now.getDayOfMonth();
        // 5.鍐欏叆Redis SETBIT key offset 1
        stringRedisTemplate.opsForValue().setBit(key, dayOfMonth - 1, true);
        return Result.ok();
    }

    @Override
    public Result signCount() {
        // 1.鑾峰彇褰撳墠鐧诲綍鐢ㄦ埛
        Long userId = UserHolder.getUser().getId();
        // 2.鑾峰彇鏃ユ湡
        LocalDateTime now = LocalDateTime.now();
        // 3.鎷兼帴key
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = USER_SIGN_KEY + userId + keySuffix;
        // 4.鑾峰彇浠婂ぉ鏄湰鏈堢殑绗嚑澶?
        int dayOfMonth = now.getDayOfMonth();
        // 5.鑾峰彇鏈湀鎴浠婂ぉ涓烘鐨勬墍鏈夌殑绛惧埌璁板綍锛岃繑鍥炵殑鏄竴涓崄杩涘埗鐨勬暟瀛?BITFIELD sign:5:202203 GET u14 0
        List<Long> result = stringRedisTemplate.opsForValue().bitField(
                key,
                BitFieldSubCommands.create()
                        .get(BitFieldSubCommands.BitFieldType.unsigned(dayOfMonth)).valueAt(0)
        );
        if (result == null || result.isEmpty()) {
            // 娌℃湁浠讳綍绛惧埌缁撴灉
            return Result.ok(0);
        }
        Long num = result.get(0);
        if (num == null || num == 0) {
            return Result.ok(0);
        }
        // 6.寰幆閬嶅巻
        int count = 0;
        while (true) {
            // 6.1.璁╄繖涓暟瀛椾笌1鍋氫笌杩愮畻锛屽緱鍒版暟瀛楃殑鏈€鍚庝竴涓猙it浣? // 鍒ゆ柇杩欎釜bit浣嶆槸鍚︿负0
            if ((num & 1) == 0) {
                // 濡傛灉涓?锛岃鏄庢湭绛惧埌锛岀粨鏉?
                break;
            }else {
                // 濡傛灉涓嶄负0锛岃鏄庡凡绛惧埌锛岃鏁板櫒+1
                count++;
            }
            // 鎶婃暟瀛楀彸绉讳竴浣嶏紝鎶涘純鏈€鍚庝竴涓猙it浣嶏紝缁х画涓嬩竴涓猙it浣?
            num >>>= 1;
        }
        return Result.ok(count);
    }

    private User createUserWithPhone(String phone) {
        // 1.鍒涘缓鐢ㄦ埛
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        // 2.淇濆瓨鐢ㄦ埛
        save(user);
        return user;
    }
}
