package com.pinyougou.cart.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;

import entity.Result;
import util.IdWorker;

@RestController
@RequestMapping("/pay")
public class PayController {

	@Reference
	private WeixinPayService weixinPayService;
	@Autowired
	private RedisTemplate redisTemplate;
	@Autowired
	private OrderService orderService;
	
	@RequestMapping("/createNative")
	public Map createNative() {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		TbPayLog payLog = (TbPayLog) redisTemplate.boundHashOps("payLog").get(username);
		if (payLog != null) {
			return weixinPayService.createNative(payLog.getOutTradeNo(), payLog.getTotalFee()+"");
		}
		return new HashMap<>();
	}
	
	/**
	 * 向微信平台查询支付状态
	 * @param out_trade_no
	 * @return
	 */
	@RequestMapping("/queryPayStatus")
	public Result queryPayStatus(String out_trade_no) {
		Result result = null;
		int times = 0; //问询次数
		while(true) {
			Map<String, String> map = weixinPayService.queryPayStatus(out_trade_no);
			if (map == null) {
				result = new Result(false, "支付出错");
				break;
			}
			if (map.get("trade_state").equals("SUCCESS")) {//如果成功
				result = new Result(true, "支付成功");
				orderService.updateOrderStatus(out_trade_no, map.get("transaction_id"));
				break;
			}
			
			try {
				Thread.sleep(3000);  //间隔三秒问询
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if (++times > 100) {
				result = new Result(false, "支付超时");
				break;
			}
		}
		
		return result;
	}
}
