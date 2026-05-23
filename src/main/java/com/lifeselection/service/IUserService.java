package com.lifeselection.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lifeselection.dto.LoginFormDTO;
import com.lifeselection.dto.Result;
import com.lifeselection.entity.User;

import javax.servlet.http.HttpSession;

/**
 * <p>
 *  鏈嶅姟绫?
 * </p>
 *
 * @author 铏庡摜
 * @since 2021-12-22
 */
public interface IUserService extends IService<User> {

    Result sendCode(String phone, HttpSession session);

    Result login(LoginFormDTO loginForm, HttpSession session);

    Result sign();

    Result signCount();

}
