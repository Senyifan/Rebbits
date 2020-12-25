package com.itheima.sso.service;

import com.itheima.common.pojo.TaotaoResult;
import com.itheima.pojo.TbUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户注册的接口
 * @author liayun
 *
 */
@Mapper
public interface UserRegisterService {
	
	/**
	 * 根据参数和类型来进行校验数据
	 * @param param：要校验的数据
	 * @param type：可选参数有1、2、3，分别代表username、phone、email
	 * @return
	 */
	public TaotaoResult checkData(String param, Integer type);
	
	/**
	 * 注册用户
	 * @param user
	 * @return
	 */
	public TaotaoResult register(TbUser user);

}
