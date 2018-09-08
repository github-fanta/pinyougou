package com.pinyougou.search.service.impl;

import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
@Component
public class ItemSearchListener implements MessageListener{

	@Autowired
	private ItemSearchService itemSearchService;
	@Override
	public void onMessage(Message message) {

		System.out.println("监听接收到消息。。。。");
		try {
			TextMessage textMessage = (TextMessage)message;
			String text = textMessage.getText();
			List<TbItem> itemList = (List<TbItem>) JSON.parseArray(text, TbItem.class);
			itemSearchService.importList(itemList); //导入索引库
			System.out.println("成功导入到索引库！");
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

}
