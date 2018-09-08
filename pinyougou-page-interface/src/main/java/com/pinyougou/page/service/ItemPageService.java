package com.pinyougou.page.service;

/**
 * 商品详情页接口
 * @author liq
 *
 */
public interface ItemPageService {

	/**
	 * 生成商品详情页
	 * @param goodsId
	 * @return
	 */
	public boolean genItemHtml(Long goodsId);
	
	/**
	 * 删除商品详情静态网页
	 * @param goodsIds
	 * @return
	 */
	public boolean deleteItemHtml(Long[] goodsIds);
}
