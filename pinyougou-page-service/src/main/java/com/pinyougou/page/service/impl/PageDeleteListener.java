package com.pinyougou.page.service.impl;

import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pinyougou.page.service.ItemPageService;

@Component
public class PageDeleteListener implements MessageListener {

	@Autowired
	private ItemPageService itemPageService;
	@Override
	public void onMessage(Message message) {

		try {
			System.out.println("接收删除消息");
			ObjectMessage objectMessage = (ObjectMessage)message;
			Long[] goodsIds = (Long[]) objectMessage.getObject();
			boolean success = itemPageService.deleteItemHtml(goodsIds);
			System.out.println("删除完成："+success);
		} catch (JMSException e) {
			e.printStackTrace();
		}
		
	}

}
