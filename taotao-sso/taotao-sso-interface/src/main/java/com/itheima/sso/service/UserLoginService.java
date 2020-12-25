package com.itheima.sso.service;

import com.itheima.common.pojo.TaotaoResult;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户登录的接口
 * @author liayun
 *
 */
@Mapper
public interface UserLoginService {
	
	/**
	 * 根据用户名和密码进行登录
	 * @param username
	 * @param password
	 * @return：TaotaoResult对象，登录成功，返回200，并且包含一个token数据
	 *                          登录失败，返回400
	 */
	public TaotaoResult login(String username, String password);
	
	/**
	 * 根据token获取用户的信息
	 * @param token
	 * @return TaotaoResult：应该要包含用户的信息
	 */
	public TaotaoResult getUserByToken(String token);

	/**
	 * 安全退出
	 * @param token
	 * @return
	 */
	public TaotaoResult logout(String token);
}
