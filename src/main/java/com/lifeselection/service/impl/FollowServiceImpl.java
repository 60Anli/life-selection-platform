package com.lifeselection.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lifeselection.dto.Result;
import com.lifeselection.dto.UserDTO;
import com.lifeselection.entity.Follow;
import com.lifeselection.mapper.FollowMapper;
import com.lifeselection.service.IFollowService;
import com.lifeselection.service.IUserService;
import com.lifeselection.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 鏈嶅姟瀹炵幇绫?
 * </p>
 *
 * @author 铏庡摜
 * @since 2021-12-22
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private IUserService userService;

    @Override
    public Result follow(Long followUserId, Boolean isFollow) {
        // 1.鑾峰彇鐧诲綍鐢ㄦ埛
        Long userId = UserHolder.getUser().getId();
        String key = "follows:" + userId;
        // 1.鍒ゆ柇鍒板簳鏄叧娉ㄨ繕鏄彇鍏?
        if (isFollow) {
            // 2.鍏虫敞锛屾柊澧炴暟鎹?
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(followUserId);
            boolean isSuccess = save(follow);
            if (isSuccess) {
                // 鎶婂叧娉ㄧ敤鎴风殑id锛屾斁鍏edis鐨剆et闆嗗悎 sadd userId followerUserId
                stringRedisTemplate.opsForSet().add(key, followUserId.toString());
            }
        } else {
            // 3.鍙栧叧锛屽垹闄?delete from tb_follow where user_id = ? and follow_user_id = ?
            boolean isSuccess = remove(new QueryWrapper<Follow>()
                    .eq("user_id", userId).eq("follow_user_id", followUserId));
            if (isSuccess) {
                // 鎶婂叧娉ㄧ敤鎴风殑id浠嶳edis闆嗗悎涓Щ闄?
                stringRedisTemplate.opsForSet().remove(key, followUserId.toString());
            }
        }
        return Result.ok();
    }

    @Override
    public Result isFollow(Long followUserId) {
        // 1.鑾峰彇鐧诲綍鐢ㄦ埛
        Long userId = UserHolder.getUser().getId();
        // 2.鏌ヨ鏄惁鍏虫敞 select count(*) from tb_follow where user_id = ? and follow_user_id = ?
        Integer count = query().eq("user_id", userId).eq("follow_user_id", followUserId).count();
        // 3.鍒ゆ柇
        return Result.ok(count > 0);
    }

    @Override
    public Result followCommons(Long id) {
        // 1.鑾峰彇褰撳墠鐢ㄦ埛
        Long userId = UserHolder.getUser().getId();
        String key = "follows:" + userId;
        // 2.姹備氦闆?
        String key2 = "follows:" + id;
        Set<String> intersect = stringRedisTemplate.opsForSet().intersect(key, key2);
        if (intersect == null || intersect.isEmpty()) {
            // 鏃犱氦闆?
            return Result.ok(Collections.emptyList());
        }
        // 3.瑙ｆ瀽id闆嗗悎
        List<Long> ids = intersect.stream().map(Long::valueOf).collect(Collectors.toList());
        // 4.鏌ヨ鐢ㄦ埛
        List<UserDTO> users = userService.listByIds(ids)
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());
        return Result.ok(users);
    }
}
