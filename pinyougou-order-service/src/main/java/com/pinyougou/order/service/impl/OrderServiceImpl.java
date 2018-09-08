package com.pinyougou.order.service.impl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderExample;
import com.pinyougou.pojo.TbOrderExample.Criteria;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojogroup.Cart;
import com.pinyougou.order.service.OrderService;

import entity.PageResult;
import util.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbOrder> page=   (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Autowired
	private RedisTemplate redisTemplate;
	
	@Autowired
	private IdWorker idWorker;
	@Autowired
	private TbOrderItemMapper orderItemMapper;
	@Autowired
	private TbPayLogMapper payLogMapper;
	
	/**
	 * 增加
	 */
	@Override
	public void add(TbOrder order) {
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());
		List<String> orderIdList = new ArrayList<>(); //一次支付所包含的订单
		double total_money = 0; //一次支付的总订单总金额
		for (Cart cart : cartList) {
			TbOrder newOrder = new TbOrder();
			long orderId = idWorker.nextId();
			newOrder.setOrderId(orderId); //订单ID
			newOrder.setUserId(order.getUserId()); //用户名
			newOrder.setPaymentType(order.getPaymentType());//支付方式
			newOrder.setStatus("1");//状态：未支付
			newOrder.setCreateTime(new Date());
			newOrder.setUpdateTime(new Date());
			newOrder.setReceiverAreaName(order.getReceiverAreaName()); //收货地址
			newOrder.setReceiverMobile(order.getReceiverMobile()); //联系电话
			newOrder.setReceiver(order.getReceiver());	//收货人
			newOrder.setSourceType(order.getSourceType());	//订单来源：1:app端，2：pc端，3：M端，4：微信端，5：手机qq端
			newOrder.setSellerId(cart.getSellerId()); //商家ID
		
			double momey = 0; //总金额
			for (TbOrderItem orderItem : cart.getOrderItemList()) {
				orderItem.setId(idWorker.nextId());
				orderItem.setOrderId(orderId);
				orderItem.setSellerId(cart.getSellerId());
				momey += orderItem.getTotalFee().doubleValue();//订单（一个商家）金额
				orderItemMapper.insert(orderItem);
				
				total_money += momey; //总订单金额 （单位：元RMB）
				orderIdList.add(orderItem.getId()+"");
			}
			newOrder.setPayment(new BigDecimal(momey));   
			orderMapper.insert(newOrder);
		}
		
		//创建支付记录
		if ("1".equals(order.getPaymentType())) { //支付类型，1、在线支付，2、货到付款
			TbPayLog payLog = new TbPayLog(); 
			payLog.setOutTradeNo(idWorker.nextId()+"");	//获取支付订单号
			payLog.setCreateTime(new Date());
			String orderIds = orderIdList.toString().replace("[", "").replace("", "]");
			payLog.setOrderList(orderIds);
			payLog.setTotalFee((long)total_money*100);//总订单金额 （单位：分RMB）
			payLog.setUserId(order.getUserId()); //用户ID
			payLog.setTradeState("0"); //支付状态  0：未支付
			payLog.setPayType("1"); //支付类型:微信付款
			
			payLogMapper.insert(payLog);  //插入支付记录表
			redisTemplate.boundHashOps("payLog").put(payLog.getUserId(), payLog); // 支付项放入缓存
		}
		
		redisTemplate.boundHashOps("cartList").delete(order.getUserId());
		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbOrder findOne(Long id){
		return orderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			orderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
	@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(order!=null){			
						if(order.getPaymentType()!=null && order.getPaymentType().length()>0){
				criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
			}
			if(order.getPostFee()!=null && order.getPostFee().length()>0){
				criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}
			if(order.getStatus()!=null && order.getStatus().length()>0){
				criteria.andStatusLike("%"+order.getStatus()+"%");
			}
			if(order.getShippingName()!=null && order.getShippingName().length()>0){
				criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}
			if(order.getShippingCode()!=null && order.getShippingCode().length()>0){
				criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}
			if(order.getUserId()!=null && order.getUserId().length()>0){
				criteria.andUserIdLike("%"+order.getUserId()+"%");
			}
			if(order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0){
				criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}
			if(order.getBuyerNick()!=null && order.getBuyerNick().length()>0){
				criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}
			if(order.getBuyerRate()!=null && order.getBuyerRate().length()>0){
				criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}
			if(order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0){
				criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}
			if(order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}
			if(order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0){
				criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}
			if(order.getReceiver()!=null && order.getReceiver().length()>0){
				criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}
			if(order.getInvoiceType()!=null && order.getInvoiceType().length()>0){
				criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}
			if(order.getSourceType()!=null && order.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}
			if(order.getSellerId()!=null && order.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}
	
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 根据用户查询支付记录
	 */
	@Override
	public TbPayLog searchPayLogFromRedis(String userId) {
		return (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);
	}

	/**
	 * 更新订单状态
	 */
	@Override
	public void updateOrderStatus(String out_trade_no, String transaction_id) {
		
		//1.修改支付记录
		TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
		payLog.setTransactionId(transaction_id);
		payLog.setPayTime(new Date());
		payLog.setTradeState("1"); //已支付
		payLogMapper.updateByPrimaryKey(payLog);
		
		//2.修改订单状态
		String orderList = payLog.getOrderList();
		String[] orders = orderList.split(",");
		for (String orderId : orders) {
			TbOrder order = orderMapper.selectByPrimaryKey(Long.parseLong(orderId));
			if (order != null) {
				order.setStatus("2");//已付款
				order.setPaymentTime(new Date()); //付款时间
				orderMapper.updateByPrimaryKey(order);
			}
			
		}
		
		//清除redis缓存支付数据
		redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());
		
	}
	
}
