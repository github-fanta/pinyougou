package com.pinyougou.search.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightEntry.Highlight;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbContentCategory;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;

@Service(timeout=50000)
public class ItemSearchServiceImpl implements ItemSearchService {

	@Autowired
	private SolrTemplate solrTemplate;
	
	@Override
	public Map<String, Object> search(Map searchMap) {
		
		String keywords = (String) searchMap.get("keywords");
		searchMap.put("keywords", keywords.replace(" ", ""));
		
		Map map = new HashMap();
		//高亮显示查询
		map.putAll(searchList(searchMap));
		//商品分类名称查询
		List<String> categoryList = searchCategoryList(searchMap);
		map.put("categoryList", categoryList);
		
		//查询品牌和规格列表
		String selectedCat = (String) searchMap.get("category");
		if(selectedCat.equals("")) {
			map.putAll(searchBrandAndSpecList(categoryList.get(0)));
		}else {
			map.putAll(searchBrandAndSpecList(selectedCat));
		}
		
		return map;
	}
	
	/**
	 * 查询高亮
	 * @param searchMap
	 * @return
	 */
	private Map searchList(Map searchMap) {
		
		SimpleHighlightQuery query = new SimpleHighlightQuery();
		HighlightOptions options = new HighlightOptions();
		
		//给item_title中 用户搜索的关键字 加上高亮
		//设置高亮的域  
		options.addField("item_title"); //可链式添加更多：.addField("yyy").addField("xxx")...;
		// 高亮前缀 
		options.setSimplePrefix("<em style='color:red'>");
		// 高亮后缀
		options.setSimplePostfix("</em>");
		
		// 设置高亮选项
		query.setHighlightOptions(options);
		
		//1.1关键字查询
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		
		//1.2按分类筛选
		if (!"".equals(searchMap.get("category"))) { //如果用户选择了分类
			SimpleFilterQuery filterQuery = new SimpleFilterQuery();
			Criteria filterCriteria = new Criteria("item_keywords").is(searchMap.get("category"));
			filterQuery.addCriteria(filterCriteria);
			query.addFilterQuery(filterQuery);
		}
		

		//1.3按品牌筛选
		if (!"".equals(searchMap.get("brand"))) { //如果用户选择了品牌
			SimpleFilterQuery filterQuery = new SimpleFilterQuery();
			Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
			filterQuery.addCriteria(filterCriteria);
			query.addFilterQuery(filterQuery);
		}
		
		//1.4根据规格过滤
		if (!"".equals(searchMap.get("spec"))) { //如果用户选择了规格
			Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
			for (String key : specMap.keySet()) {
				SimpleFilterQuery filterQuery = new SimpleFilterQuery();
				Criteria filterCriteria = new Criteria("item_spec_"+key).is(specMap.get(key));
				filterQuery.addCriteria(filterCriteria);
				query.addFilterQuery(filterQuery);
			}
		}
		
		//1.5根据价格过滤
		if (!"".equals(searchMap.get("price"))) {
			String priceStr = (String) searchMap.get("price");
			if (priceStr.matches("\\d+-\\d+")) {
				String[] price = priceStr.split("-");
				//添加最小价格
				if (!price[0].equals("0")) {
					SimpleFilterQuery filterQuery = new SimpleFilterQuery();
					Criteria filterCriteria = new Criteria("item_price").greaterThan(price[0]);
					filterQuery.addCriteria(filterCriteria);
					query.addFilterQuery(filterQuery);
				}
				//添加最大价格
				if (!price[1].equals("*")) {
					SimpleFilterQuery filterQuery = new SimpleFilterQuery();
					Criteria filterCriteria = new Criteria("item_price").lessThan(price[1]);
					filterQuery.addCriteria(filterCriteria);
					query.addFilterQuery(filterQuery);
				}
			}
			
		}
		
		
		//1.6分页查询
		Integer pageNo = (Integer)searchMap.get("pageNo");
		if(pageNo == null || pageNo < 1) {
			pageNo = 1;
		}
		Integer pageSize = (Integer) searchMap.get("pageSize");
		if (pageSize == null || pageSize < 1) {
			pageSize = 20;
		}
		query.setOffset((pageNo-1)*pageSize);
		query.setRows(pageSize);
		
		//1.7排序
		String sortValue = (String) searchMap.get("sort"); //升序ASC 降序DESC
		String sortField = (String) searchMap.get("sortField");//排序字段
		if (sortValue != null && !sortField.equals("")) {
			if (sortValue.equalsIgnoreCase("ASC")) {
				Sort sort = new Sort(Sort.Direction.ASC, "item_"+sortField);
				query.addSort(sort);
			}else if (sortValue.equalsIgnoreCase("DESC")) {
				Sort sort = new Sort(Sort.Direction.DESC, "item_"+sortField);
				query.addSort(sort);
			}
		}
		
		/*高亮处理开始*/
		
		HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
		
		//循环高亮入口集合（每条记录的高亮入口）
		List<HighlightEntry<TbItem>> entryList = page.getHighlighted();
		for (HighlightEntry<TbItem> entry : entryList) {
			//多个高亮字段（域）如 标题域item_title,品牌域item_brand...
			List<Highlight> highlights = entry.getHighlights();
			/*for (Highlight h : highlights) {
				List<String> sns = h.getSnipplets();	//复制域可能有多列(域)如item_keywords域包括item_title,item_category,item_seller...
				System.out.println(sns);
			}*/
			TbItem item = entry.getEntity(); //获取原实体类  和page.getContent()结果一样
			if (highlights.size() > 0 && highlights.get(0).getSnipplets().size() > 0) {
				item.setTitle(highlights.get(0).getSnipplets().get(0));//设置高亮结果
			}
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("rows", page.getContent());
		
		map.put("total", page.getTotalElements());//返回总记录数
		map.put("totalPages", page.getTotalPages());//返回总页数
		return map;
	}

	/**
	 * 根据搜索命查找商品分类
	 * @param searchMap
	 * @return
	 */
	private List searchCategoryList(Map searchMap) {
		
		List resultList = new ArrayList<>();
		
		SimpleQuery query = new SimpleQuery("*:*");
		//按照关键字查找
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));//where....子句
		query.addCriteria(criteria);
		//设置分组选项
		GroupOptions groupOptions = new GroupOptions();
		groupOptions.addGroupByField("item_category");	//group by...子句
		query.setGroupOptions(groupOptions);
		//得到分组页
		GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
		//根据列名得到分组结果集
		GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
		//得到分组结果入口页
		Page<GroupEntry<TbItem>> entries = groupResult.getGroupEntries();
		//得到分组入口集合
		List<GroupEntry<TbItem>> entryList = entries.getContent();
		for (GroupEntry<TbItem> groupEntry : entryList) {
			resultList.add(groupEntry.getGroupValue());//将分组结果的名称封装到返回值中	
		}
		return resultList;
	}
	
	@Autowired
	private RedisTemplate redisTemplate;
	/**
	 * 从缓存中查找商品分类名关联的模板中的所有品牌和规格列表
	 * @param category
	 * @return map
	 */
	private Map searchBrandAndSpecList(String category) {
		
		Map<Object, Object> map = new HashMap<>();
		Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
		if (typeId != null) {
			List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
			map.put("brandList", brandList);
			List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
			map.put("specList", specList);
		}
		return map;
	}

	//载入搜索库中
	@Override
	public void importList(List<TbItem> itemList) {
		solrTemplate.saveBeans(itemList);
		solrTemplate.commit();
	}

	//删除索引库中goodsIds
	@Override
	public void deleteByGoodsIds(List goodsIds) {
				
		SimpleQuery query=new SimpleQuery("*:*");		
		Criteria criteria=new Criteria("item_goodsid").in(goodsIds);
		query.addCriteria(criteria);		
		solrTemplate.delete(query);
		solrTemplate.commit();
	}
}
