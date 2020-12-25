package com.itheima.sso.service.impl;

import com.itheima.common.pojo.TaotaoResult;
import com.itheima.mapper.TbUserMapper;
import com.itheima.pojo.TbUser;
import com.itheima.pojo.TbUserExample;
import com.itheima.pojo.TbUserExample.Criteria;
import com.itheima.sso.service.UserRegisterService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.List;

@Service
public class UserRegisterServiceImpl implements UserRegisterService {

	@Autowired
	private TbUserMapper userMapper;
	
	@Override
	public TaotaoResult checkData(String param, Integer type) {
		// 1. 注入Mapper
		// 2. 根据参数动态的生成查询条件
		TbUserExample example = new TbUserExample();
		Criteria criteria = example.createCriteria();
		if (type == 1) { // 如果type为1，那么表示的是username
			// 注意，username不能为空，如果username为空的话，那么程序就不需要往下走了
			if (StringUtils.isEmpty(param)) {
				return TaotaoResult.ok(false);
			}
			criteria.andUsernameEqualTo(param);
		} else if (type == 2) { // 如果type为2，那么表示的是phone
			criteria.andPhoneEqualTo(param);
		} else if (type == 3) { // 如果type为3，那么表示的是email
			criteria.andEmailEqualTo(param);
		} else { // 是非法参数
			// return 非法的参数
			return TaotaoResult.build(400, "非法的参数");
		}
		// 3. 调用Mapper的查询方法获取数据
		List<TbUser> list = userMapper.selectByExample(example);
		// 4. 如果查询到了数据，则说明数据不可用，返回的是false
		if (list != null && list.size() > 0) {
			return TaotaoResult.ok(false);
		}
		// 5. 如果没查询到数据，则说明数据是可用的，返回的是true
		return TaotaoResult.ok(true);
	}

	@Override
	public TaotaoResult register(TbUser user) {
		// 1. 注入Mapper
		// 2. 校验数据
		// 2.1 校验用户名和密码不能为空，如果为空的话，那么程序就不需要往下走了
		if (StringUtils.isEmpty(user.getUsername())) {
			return TaotaoResult.build(400, "注册失败，请校验数据之后再提交数据");
		}
		if (StringUtils.isEmpty(user.getPassword())) {
			return TaotaoResult.build(400, "注册失败，请校验数据之后再提交数据");
		}
		// 2.2 校验用户名是否被注册了
		TaotaoResult result = checkData(user.getUsername(), 1);
		if (!(boolean)result.getData()) {
			// 数据不可用
			return TaotaoResult.build(400, "注册失败，请校验数据之后再提交数据");
		}
		// 2.3 校验电话号码是否被注册了，只有电话号码不为空的时候，才需要校验
		if (StringUtils.isNotBlank(user.getPhone())) {
			TaotaoResult result2 = checkData(user.getPhone(), 2);
			if (!(boolean)result2.getData()) {
				// 数据不可用
				return TaotaoResult.build(400, "注册失败，请校验数据之后再提交数据");
			}
		}
		// 2.4 校验email是否被注册了，只有email不为空的时候，才需要校验
		if (StringUtils.isNotBlank(user.getEmail())) {
			TaotaoResult result2 = checkData(user.getEmail(), 3);
			if (!(boolean)result2.getData()) {
				// 数据不可用
				return TaotaoResult.build(400, "注册失败，请校验数据之后再提交数据");
			}
		}
		// 3. 如果校验成功，那么就需要补全其他的属性，因为传递过来的只有4个属性
		user.setCreated(new Date());
		user.setUpdated(user.getCreated());
		
		// 4. 对密码进行MD5加密
		String md5Password = DigestUtils.md5DigestAsHex(user.getPassword().getBytes());
		user.setPassword(md5Password);
		// 5. 插入数据
		userMapper.insertSelective(user);
		// 6. 返回TaotaoResult对象
		return TaotaoResult.ok();
	}

}
