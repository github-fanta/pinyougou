package com.pinyougou.sellergoods.service.impl;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.mapper.TbSellerMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbGoodsExample;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.pojo.TbItemExample;
import com.pinyougou.pojo.TbSeller;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	
	@Autowired
	private TbGoodsDescMapper goodsDescMapper;
	

	@Autowired
	private TbBrandMapper tbBrandMapper;
	@Autowired
	private TbItemCatMapper tbItemCatMapper;
	@Autowired
	private TbSellerMapper tbSellerMapper;
	@Autowired
	private TbItemMapper tbItemMapper;
	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		
		goods.getGoods().setAuditStatus("0"); // 0:未审核
		goodsMapper.insert(goods.getGoods());	
		
		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
		goodsDescMapper.insert(goods.getGoodsDesc());
		
		saveItemList(goods);
		
	}

	//插入标题  (货物名+所有规格项)
	private void saveItemList(Goods goods){
		if(goods.getGoods().getIsEnableSpec().equals("1")) {
			for(TbItem item : goods.getItemList()) {
				//标题title 货物名+所有规格项            三星 Note II (N7100) 云石白 联通3G手机
				StringBuffer title = new StringBuffer();
				title.append(goods.getGoods().getGoodsName());
				Map<String, Object> specMap = JSON.parseObject(item.getSpec());
				for (String key : specMap.keySet()) {
					title.append(" ").append(specMap.get(key));
				}
				item.setTitle(title.toString());
				setItemValues(goods,item);
				tbItemMapper.insert(item);
			}
		}else {
			TbItem item = new TbItem();
			item.setTitle(goods.getGoods().getGoodsName());  //商品KPU
			item.setSpec("{11}");
			item.setPrice(goods.getGoods().getPrice());//价格
			item.setStatus("1");//状态
			item.setIsDefault("1");  //默认
			item.setNum(99999);//库存数量
			setItemValues(goods,item);
			tbItemMapper.insert(item);
		}
	}
	
	private void setItemValues(Goods goods,TbItem item) {
		
		//商品SPU编号 goodsId
		item.setGoodsId(goods.getGoods().getId());
		//商家编号 sellerId         560
		item.setSellerId(goods.getGoods().getSellerId());
		//商品三级分类	categoryId
		item.setCategoryid(goods.getGoods().getCategory3Id());
		
		//创建日期
		item.setCreateTime(new Date());
		//修改日期
		item.setUpdateTime(new Date());
		
		
		//品牌名称	brandId()
		TbBrand brand = tbBrandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
		item.setBrand(brand.getName());
		
		//分类名称 category 
		TbItemCat itemCat = tbItemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
		item.setCategory(itemCat.getName());
		
		//商家名称 seller
		TbSeller seller = tbSellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
		item.setSeller(seller.getNickName());
		
		//图片地址 image  (取第一张图片)
		List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(),Map.class);
		if (imageList.size() > 0) {
			//选第一张图作为item的图片
			item.setImage((String) imageList.get(0).get("url"));
		}
	}
	
	
	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){
		
		goodsMapper.updateByPrimaryKey(goods.getGoods());	
		goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());
		
		//先删除全部列表项
		TbItemExample example = new TbItemExample();
		com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(goods.getGoods().getId());
		tbItemMapper.deleteByExample(example);
		//再添加新的列表项
		saveItemList(goods);
		
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		
		Goods goods = new Goods();
		TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
		goods.setGoods(tbGoods);
		
		TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
		goods.setGoodsDesc(tbGoodsDesc);
		
		
		TbItemExample example = new TbItemExample();
		com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(id);
		List<TbItem> itemList = tbItemMapper.selectByExample(example);
		goods.setItemList(itemList);
		return goods;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		
		
		
		for(Long id:ids){
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setIsDelete("1");  //NULL:正常  1：逻辑删除
			goodsMapper.updateByPrimaryKey(goods);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		criteria.andIsDeleteIsNull();
		
		if(goods!=null){			
						if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				criteria.andSellerIdEqualTo(goods.getSellerId());  //商家货物精准查询
			}
			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}
			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}
			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}
			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}
			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}
			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}
			if(goods.getIsDelete()!=null && goods.getIsDelete().length()>0){
				criteria.andIsDeleteLike("%"+goods.getIsDelete()+"%");
			}
	
		}
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
		
	//更新审核状态

	@Override
	public void updateStatus(Long[] selectIds, String status) {
		for (Long id : selectIds) {
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setAuditStatus(status);
			goodsMapper.updateByPrimaryKey(goods);
		}
	}

	//根据商品ID和状态查询Item表信息
	@Override
	public List<TbItem> findItemByGoodsIdAndStatus(Long[] goodsId, String Status) {
		
		TbItemExample example = new TbItemExample();
		com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdIn(Arrays.asList(goodsId));
		criteria.andStatusEqualTo(Status);  // 1: 审核通过

		return tbItemMapper.selectByExample(example);
	}
	
}
