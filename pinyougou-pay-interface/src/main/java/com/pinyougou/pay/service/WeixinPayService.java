package com.pinyougou.pay.service;


import java.util.Map;
/**
 * 微信支付接口
 * @author liq
 *
 */
public interface WeixinPayService {
	/**
	 * 生成微信支付二维码
	 * @param out_trade_no
	 * @param total_fee
	 * @return
	 */
	public Map createNative(String out_trade_no, String total_fee);
	
	/**
	 * 查询订单状态
	 * @param out_trade_no
	 * @return
	 */
	public Map<String, String> queryPayStatus(String out_trade_no);
}
