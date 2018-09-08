package com.pinyougou.sellergoods.service;

import java.util.List;
import java.util.Map;

import com.pinyougou.pojo.TbBrand;

import entity.PageResult;

/**
 * 品牌接口
 * @author liq
 *
 */
public interface BrandService {

	public List<TbBrand> findAll();
	
	/**
	 * 查找分页
	 * @param pageNum 当前页
	 * @param pageSize 每页记录数
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);
	/**
	 * 添加
	 * @param tbBrand
	 */
	public void add(TbBrand tbBrand);
	
	/**
	 * 查找
	 * @param id
	 * @return
	 */
	public TbBrand findOne(long id);
	
	/**
	 * 更新
	 * @param tbBrand
	 */
	public void update(TbBrand tbBrand);
	
	/**
	 * 删除
	 * @param ids
	 */
	public void delete(long[] ids);
	
	/**
	 * 查找
	 * @return  查询结果
	 */
	public PageResult search(TbBrand tbBrand, int pageNum, int pageSize);
	
	public List<Map> selectOptionList();
}
