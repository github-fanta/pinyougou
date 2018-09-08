package com.pinyougou.user.service.impl;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbUserMapper;
import com.pinyougou.pojo.TbUser;
import com.pinyougou.pojo.TbUserExample;
import com.pinyougou.pojo.TbUserExample.Criteria;
import com.pinyougou.user.service.UserService;

import entity.PageResult;
import util.PhoneFormatCheckUtils;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class UserServiceImpl implements UserService {

	//用户注册来源
	public static enum SourceType{
		PC(1), IOS(2), ANDROID(3), OTHER(4);
		int value;
		SourceType(int value) {
			this.value = value;
		}
		public int getValue() {
			return value;
		}
	}
	@Autowired
	private TbUserMapper userMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbUser> findAll() {
		return userMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbUser> page=   (Page<TbUser>) userMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbUser user) {
		
		user.setCreated(new Date());  								//创建时间
		user.setUpdated(new Date());								//更新时间
		user.setSourceType(SourceType.PC.getValue()+""); 			//1: PC端
		user.setPassword(DigestUtils.md5Hex(user.getPassword())); 	//密码进行md5加密
		userMapper.insert(user);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbUser user){
		userMapper.updateByPrimaryKey(user);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbUser findOne(Long id){
		return userMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			userMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbUser user, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbUserExample example=new TbUserExample();
		Criteria criteria = example.createCriteria();
		
		if(user!=null){			
						if(user.getUsername()!=null && user.getUsername().length()>0){
				criteria.andUsernameLike("%"+user.getUsername()+"%");
			}
			if(user.getPassword()!=null && user.getPassword().length()>0){
				criteria.andPasswordLike("%"+user.getPassword()+"%");
			}
			if(user.getPhone()!=null && user.getPhone().length()>0){
				criteria.andPhoneLike("%"+user.getPhone()+"%");
			}
			if(user.getEmail()!=null && user.getEmail().length()>0){
				criteria.andEmailLike("%"+user.getEmail()+"%");
			}
			if(user.getSourceType()!=null && user.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+user.getSourceType()+"%");
			}
			if(user.getNickName()!=null && user.getNickName().length()>0){
				criteria.andNickNameLike("%"+user.getNickName()+"%");
			}
			if(user.getName()!=null && user.getName().length()>0){
				criteria.andNameLike("%"+user.getName()+"%");
			}
			if(user.getStatus()!=null && user.getStatus().length()>0){
				criteria.andStatusLike("%"+user.getStatus()+"%");
			}
			if(user.getHeadPic()!=null && user.getHeadPic().length()>0){
				criteria.andHeadPicLike("%"+user.getHeadPic()+"%");
			}
			if(user.getQq()!=null && user.getQq().length()>0){
				criteria.andQqLike("%"+user.getQq()+"%");
			}
			if(user.getIsMobileCheck()!=null && user.getIsMobileCheck().length()>0){
				criteria.andIsMobileCheckLike("%"+user.getIsMobileCheck()+"%");
			}
			if(user.getIsEmailCheck()!=null && user.getIsEmailCheck().length()>0){
				criteria.andIsEmailCheckLike("%"+user.getIsEmailCheck()+"%");
			}
			if(user.getSex()!=null && user.getSex().length()>0){
				criteria.andSexLike("%"+user.getSex()+"%");
			}
	
		}
		
		Page<TbUser> page= (Page<TbUser>)userMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

		@Autowired
		private RedisTemplate redisTemplate;
		@Autowired
		private JmsTemplate jmsTemplate;
		@Autowired
		private Destination smsDestination;
		
		@Value("${mobile}")
		private String mobile;
		@Value("${template_code}")
		private String template_code;
		@Value("${sign_name}")
		private String sign_name;
		@Override
		public void createSmsCode(String phone) {
			StringBuffer sb = new StringBuffer();
			for(int i=0; i<6; i++) {
				sb.append((int)(Math.random()*10));
			}
			final String code = sb.toString();	//生成6位数随机验证码
			redisTemplate.boundHashOps("smscode").put(phone, code); //放入缓存
			//发送消息队
			jmsTemplate.send(smsDestination, new MessageCreator() {
				
				@Override
				public Message createMessage(Session session) throws JMSException {
					MapMessage mapMessage = session.createMapMessage();
					
					mapMessage.setString("mobile", mobile);                 //发送手机号
					System.out.println(mapMessage.getString("mobile").toString());
					mapMessage.setString("template_code", template_code);   //短信模板号
					String sign_name_s = "";
					try {
						sign_name_s = new String(sign_name.getBytes("ISO-8859-1"), "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					mapMessage.setString("sign_name", sign_name_s);			//短信签名
					Map<String, String> map = new HashMap<String, String>();
					System.out.println(code);
					map.put("code", code);											//验证码
					System.out.println(JSON.toJSONString(map));
					mapMessage.setString("param", JSON.toJSONString(map));
					return mapMessage;
				}
			});
		}

		/**
		 * 验证码校验
		 */
		@Override
		public boolean checkSmsCode(String phone, String code) {
			String sysCode = (String) redisTemplate.boundHashOps("smscode").get(phone);//获取缓存中验证码
			if (sysCode != null && sysCode.equals(code)) {
				return true;
			}
			return false;
		}


	
}
