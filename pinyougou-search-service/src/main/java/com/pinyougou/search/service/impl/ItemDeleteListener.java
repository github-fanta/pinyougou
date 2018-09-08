package com.pinyougou.search.service.impl;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pinyougou.search.service.ItemSearchService;
@Component
public class ItemDeleteListener implements MessageListener{

	@Autowired
	private ItemSearchService itemSearchService;
	@Override
	public void onMessage(Message message) {
		try {
			ObjectMessage objectMessage = (ObjectMessage)message;
			Long[]  goodsIds = (Long[]) objectMessage.getObject();
			for (Long long1 : goodsIds) {
				System.out.println(long1);
			}
			System.out.println("监听收到删除消息。。。。"+goodsIds.toString());
			List<Long> list = Arrays.asList(goodsIds);
			for (Long long1 : list) {
				System.out.println(long1);
			}
			itemSearchService.deleteByGoodsIds(list);
			System.out.println("成功删除索引库中的记录");
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

}
