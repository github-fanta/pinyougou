package com.pinyougou.pay.service.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.springframework.beans.factory.annotation.Value;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.pay.service.WeixinPayService;

import util.HttpClient;

@Service
public class WeixinPayServiceImpl implements WeixinPayService{

	
	@Value("${appid}")
	private String appid;
	@Value("${partner}")
	private String partner;
	@Value("${partnerkey}")
	private String partnerkey;
	
	/**
	 * 生成二维码
	 * @param out_trade_no
	 * @param total_fee
	 * @return
	 */
	@Override
	public Map createNative(String out_trade_no, String total_fee) {

		//创建参数
		Map<String, String> param = new HashMap<>();
		param.put("appid", appid);		//公共号
		param.put("mch_id", partner);	//商户号
		param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
		param.put("body", "品优购"); //商品描述
		param.put("out_trade_no", out_trade_no);//商户订单号
		param.put("total_fee", total_fee);//订单金额（单位：RMB分）
		param.put("spbill_create_ip", "127.0.0.1");//终端IP 此字段没什么用，只是记录请求的IP
		param.put("trade_type", "NATIVE");//交易类型
		
		try {
			String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
			System.out.println(xmlParam);
			HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
			client.setHttps(true);
			client.setXmlParam(xmlParam);
			client.post();
			//3.获得结果
			String result = client.getContent();
			System.out.println(result);
			Map<String, String> resultMap = WXPayUtil.xmlToMap(result); //可能包含过多信息
			Map<String, String> map = new HashMap<>();
			map.put("code_url", resultMap.get("code_url")); //支付地址
			map.put("total_fee", resultMap.get("total_fee")); //总金额
			map.put("out_trade_no", out_trade_no); //订单号
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			return new HashMap<>();
		}
	}

	/**
	 * 查询订单状态
	 */
	@Override
	public Map<String, String> queryPayStatus(String out_trade_no) {

		Map<String, String> param = new HashMap<>();
		param.put("appid", appid);
		param.put("mch_id", partner);
		param.put("out_trade_no ", out_trade_no );
		param.put("nonce_str ", WXPayUtil.generateNonceStr());
		
		try {
			String xmlParam = WXPayUtil.generateSignature(param, partnerkey);
			HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
			httpClient.setHttps(true);
			httpClient.setXmlParam(xmlParam);
			httpClient.post();
			String xmlResult = httpClient.getContent();
			Map<String, String> map = WXPayUtil.xmlToMap(xmlResult);
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
