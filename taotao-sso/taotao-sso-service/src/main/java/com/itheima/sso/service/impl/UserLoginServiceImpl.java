package com.itheima.sso.service.impl;

import com.itheima.common.pojo.TaotaoResult;
import com.itheima.common.utils.JsonUtils;
import com.itheima.mapper.TbUserMapper;
import com.itheima.pojo.TbUser;
import com.itheima.pojo.TbUserExample;
import com.itheima.pojo.TbUserExample.Criteria;
import com.itheima.sso.jedis.JedisClient;
import com.itheima.sso.service.UserLoginService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.List;
import java.util.UUID;

@Service
public class UserLoginServiceImpl implements UserLoginService {


	@Autowired
	private TbUserMapper userMapper;
	
	@Autowired
	private JedisClient client;
	
	@Value("${USER_INFO}")
	private String USER_INFO;
	
	@Value("${EXPIRE_TIME}")
	private Integer EXPIRE_TIME;

	@Override
	public TaotaoResult login(String username, String password) {
		// 1. 注入Mapper
		// 2. 校验用户名和密码是否为空
		if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
			return TaotaoResult.build(400, "用户名或密码错误");
		}
		// 3. 先校验用户名
		TbUserExample example = new TbUserExample();
		Criteria criteria = example.createCriteria();
		criteria.andUsernameEqualTo(username);
		List<TbUser> list = userMapper.selectByExample(example); // 发送的sql语句：select * from tb_user where username = zhangsan
		if (list == null && list.size() == 0) {
			return TaotaoResult.build(400, "用户名或密码错误");
		}
		// 4. 再校验密码
		TbUser user = list.get(0);
		// 先对密码进行MD5加密再比较
		String md5DigestAsHex = DigestUtils.md5DigestAsHex(password.getBytes());
		if (!md5DigestAsHex.equals(user.getPassword())) { // 表示用户的密码不正确
			return TaotaoResult.build(400, "用户名或密码错误");
		}
		/*
		 * 5. 如果校验成功，那么先生成token（即根据UUID算法生成的key），还需要设置token的有效期来模拟Session，
		 *    token存在哪儿？用户的数据是存在Redis数据库里面的，key为token，value为代表用户信息的json数据。
		 *    再将token设置到Cookie当中（在表现层中设置，而且Cookie还需要跨越）
		 */
		String token = UUID.randomUUID().toString();
		/*
		 * 大家想一想，我们到时候这个key的数据是不是要校验啊，校验的时候，
		 * 如果说你把用户的数据都存放在Redis数据库中了，那么很显然密码也会存进去。这样做并不好，因为太危险了！！！
		 * 
		 * 最好密码就设置为空，不要把密码存进去，因为将来我们校验身份的时候，要获取用户的数据，
		 * 这时就会把密码拿到，虽然它是加密的， 但是也不好，所以要把密码设置为空
		 */
		user.setPassword(null); // 为了安全，就不要把密码保存到Redis数据库里面去了，因为这样太危险了，因此我们先把密码置空
		// 使用Jedis客户端设置存放用户数据到Redis数据库中，而且为了更好地管理key，我们最好加一个前缀，例如"session:token"
		client.set(USER_INFO + ":" + token, JsonUtils.objectToJson(user));
		// 设置token的有效期来模拟Session，一般是半个小时
		client.expire(USER_INFO + ":" + token, EXPIRE_TIME);
		return TaotaoResult.ok(token);
	}

	@Override
	public TaotaoResult getUserByToken(String token) {
		// 1. 注入JedisClient
		// 2. 根据token查询用户信息（JSON格式的）
		String strjson = client.get(USER_INFO + ":" + token);
		// 3. 判断是否能够查询到
		if (StringUtils.isNotBlank(strjson)) {
			// 4. 如果查询到了，那么返回200，而且还要包含用户的信息（用户信息应该转成一个对象）
			TbUser user = JsonUtils.jsonToPojo(strjson, TbUser.class);
			// 重新设置过期时间
			client.expire(USER_INFO + ":" + token, EXPIRE_TIME);
			return TaotaoResult.ok(user);
		}
		// 5. 如果查询不到，那么返回400
		return TaotaoResult.build(400, "用户已过期");
	}

	@Override
	public TaotaoResult logout(String token) {
		client.expire(USER_INFO + ":" + token, 0);
		return TaotaoResult.ok();
	}

}
