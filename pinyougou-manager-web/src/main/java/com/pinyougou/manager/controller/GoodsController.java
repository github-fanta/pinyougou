package com.pinyougou.manager.controller;
import java.util.Arrays;
import java.util.List;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import entity.Result;
/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;
	
	/*@Reference(timeout=100000)
	private ItemSearchService searchService;*/
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return goodsService.findPage(page, rows);
	}
	

	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@Autowired
	private Destination queueSolrDeleteDestination;//点对点队列 用于导入/删除索引
	@Autowired
	private Destination topicPageDeleteDestination;//发布/订阅队列 删除商品详情页
	@RequestMapping("/delete")
	public Result delete(final Long [] ids){
		try {
			//searchService.deleteByGoodsIds(Arrays.asList(ids));
			jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
				
				@Override
				public Message createMessage(Session session) throws JMSException {
					
					return session.createObjectMessage(ids);
				}
			});
			goodsService.delete(ids);
			
			//删除商品详情页
			jmsTemplate.send(topicPageDeleteDestination, new MessageCreator() {
				
				@Override
				public Message createMessage(Session session) throws JMSException {
					return session.createObjectMessage(ids);
				}
			});
			
			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param brand
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
		return goodsService.findPage(goods, page, rows);		
	}
	
	@Autowired
	private Destination queueSolrDestination;
	@Autowired
	private Destination topicPageDestination;//发布订阅 用于生成/删除商品详情页的静态页面  
	
	@Autowired
	private JmsTemplate jmsTemplate;
	
	//更新审核状态
	@RequestMapping("/updateStatus")
	public Result updateStatus( Long[] selectIds, String status) {
		
		try {
			goodsService.updateStatus(selectIds,status);
			//按照SPU ID查询 SKU列表(状态为1)
			if ("1".equals(status)) {  //1 :审核通过
				//获取需要导入的SKU列表
				List<TbItem> itemList = goodsService.findItemByGoodsIdAndStatus(selectIds, status);
				//导入到solr
				if (itemList.size() > 0) {
					//searchService.importList(itemList);
					final String jsonString = JSON.toJSONString(itemList);
					jmsTemplate.send(queueSolrDestination, new MessageCreator() {
						
						@Override
						public Message createMessage(Session session) throws JMSException {
							return session.createTextMessage(jsonString);
						}
					});
				}
				
				//生成商品详情页
				for (final Long goodsId : selectIds) {
					jmsTemplate.send(topicPageDestination, new MessageCreator() {
						
						@Override
						public Message createMessage(Session session) throws JMSException {
							return session.createTextMessage(""+goodsId);
						}
					});
				}

			}
			
			/*//生成静态页面
			for (Long id : selectIds) {
				itemPageService.genItemHtml(id);
			}*/
			
			return new Result(true, "审核成功");
		}catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "审核失败");
		}
	}
	


	
}
