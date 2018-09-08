package com.pinyougou.page.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbItemExample.Criteria;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateNotFoundException;

@Service
public class ItemPageServiceImpl implements ItemPageService {

	//模板位置
	@Value("${pagedir}")
	private String pagedir;
	
	@Autowired
	private FreeMarkerConfig freeMarkerConfig;
	@Autowired
	private TbGoodsMapper goodsMapper;
	@Autowired
	private TbGoodsDescMapper goodsDescMapper;
	@Autowired
	private TbItemCatMapper TbItemCatMapper;
	@Autowired
	private TbItemMapper tbItemMapper;
	@Override
	public boolean genItemHtml(Long goodsId) {
		
		try {
			Configuration configuration = freeMarkerConfig.getConfiguration();
			Template template = configuration.getTemplate("item.ftl");
			FileWriter out = new FileWriter(pagedir+goodsId+".html");
			HashMap<String, Object> dataModel = new HashMap<>();
			
			//1.加载商品表数据
			TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
			dataModel.put("goods", goods);
			//2.加载商品扩展表数据	
			TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
			dataModel.put("goodsDesc",goodsDesc);
			//3.加载商品分类
			String itemCat1 = TbItemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
			String itemCat2 = TbItemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
			String itemCat3 = TbItemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
			dataModel.put("itemCat1", itemCat1);
			dataModel.put("itemCat2", itemCat2);
			dataModel.put("itemCat3", itemCat3);
			//4.SKU列表
			TbItemExample example = new TbItemExample();
			Criteria criteria = example.createCriteria();
			criteria.andGoodsIdEqualTo(goodsId);
			criteria.andStatusEqualTo("1"); //启用
			example.setOrderByClause("is_default DESC");
			List<TbItem> itemList = tbItemMapper.selectByExample(example);
			dataModel.put("itemList", itemList);
			
			template.process(dataModel, out);
			out.close();
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		return false;
	}

	/**
	 * 删除商品详情静态网页
	 * @param goodsIds
	 */
	public boolean deleteItemHtml(Long[] goodsIds){
		
		try {
			for (Long goodsId : goodsIds) {
				new File(pagedir +String.valueOf(goodsId)+ ".html").delete();
			} 
			return true;
		} catch (Exception e) {
			return false;
		}
		
	}
}
