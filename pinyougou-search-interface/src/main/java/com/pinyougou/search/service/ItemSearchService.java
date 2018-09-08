package com.pinyougou.search.service;

import java.util.List;
import java.util.Map;

import com.pinyougou.pojo.TbItem;


public interface ItemSearchService {
	
	public Map<String, Object> search(Map searchMap);
	
	//将item载入搜索库中
	public void importList(List<TbItem> itemList);
	
	//按照goodsId从solr库中删除
	public void deleteByGoodsIds(List<Long> goodsIds);
}
