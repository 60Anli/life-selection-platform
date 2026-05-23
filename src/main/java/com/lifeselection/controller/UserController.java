package com.lifeselection.controller;


import cn.hutool.core.bean.BeanUtil;
import com.lifeselection.dto.LoginFormDTO;
import com.lifeselection.dto.Result;
import com.lifeselection.dto.UserDTO;
import com.lifeselection.entity.User;
import com.lifeselection.entity.UserInfo;
import com.lifeselection.service.IUserInfoService;
import com.lifeselection.service.IUserService;
import com.lifeselection.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 * <p>
 * 鍓嶇鎺у埗鍣?
 * </p>
 *
 * @author 铏庡摜
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    @Resource
    private IUserInfoService userInfoService;

    /**
     * 鍙戦€佹墜鏈洪獙璇佺爜
     */
    @PostMapping("code")
    public Result sendCode(@RequestParam("phone") String phone, HttpSession session) {
        // 鍙戦€佺煭淇￠獙璇佺爜骞朵繚瀛橀獙璇佺爜
        return userService.sendCode(phone, session);
    }

    /**
     * 鐧诲綍鍔熻兘
     * @param loginForm 鐧诲綍鍙傛暟锛屽寘鍚墜鏈哄彿銆侀獙璇佺爜锛涙垨鑰呮墜鏈哄彿銆佸瘑鐮?
     */
    @PostMapping("/login")
    public Result login(@RequestBody LoginFormDTO loginForm, HttpSession session){
        // 瀹炵幇鐧诲綍鍔熻兘
        return userService.login(loginForm, session);
    }

    /**
     * 鐧诲嚭鍔熻兘
     * @return 鏃?
     */
    @PostMapping("/logout")
    public Result logout(){
        // TODO 瀹炵幇鐧诲嚭鍔熻兘
        return Result.fail("功能未完成");
    }

    @GetMapping("/me")
    public Result me(){
        // 鑾峰彇褰撳墠鐧诲綍鐨勭敤鎴峰苟杩斿洖
        UserDTO user = UserHolder.getUser();
        return Result.ok(user);
    }

    @GetMapping("/info/{id}")
    public Result info(@PathVariable("id") Long userId){
        // 鏌ヨ璇︽儏
        UserInfo info = userInfoService.getById(userId);
        if (info == null) {
            // 娌℃湁璇︽儏锛屽簲璇ユ槸绗竴娆℃煡鐪嬭鎯?
            return Result.ok();
        }
        info.setCreateTime(null);
        info.setUpdateTime(null);
        // 杩斿洖
        return Result.ok(info);
    }

    @GetMapping("/{id}")
    public Result queryUserById(@PathVariable("id") Long userId){
        // 鏌ヨ璇︽儏
        User user = userService.getById(userId);
        if (user == null) {
            return Result.ok();
        }
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        // 杩斿洖
        return Result.ok(userDTO);
    }

    @PostMapping("/sign")
    public Result sign(){
        return userService.sign();
    }

    @GetMapping("/sign/count")
    public Result signCount(){
        return userService.signCount();
    }
}
