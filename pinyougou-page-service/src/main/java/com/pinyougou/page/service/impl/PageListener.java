package com.pinyougou.page.service.impl;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pinyougou.page.service.ItemPageService;

@Component
public class PageListener implements MessageListener {
	
	@Autowired
	private ItemPageService itemPageService;
	@Override
	public void onMessage(Message message) {
		System.out.println("处理生成页面请求。。。。");
		try {
			TextMessage textMessage = (TextMessage)message;
			String goodsId = textMessage.getText();
			System.out.println("接收到消息："+goodsId);
			boolean success = itemPageService.genItemHtml(Long.parseLong(goodsId));
			System.out.println("是否成功处理生成页面请求："+success);
		} catch (JMSException e) {
			e.printStackTrace();
		}
		
	}

}
