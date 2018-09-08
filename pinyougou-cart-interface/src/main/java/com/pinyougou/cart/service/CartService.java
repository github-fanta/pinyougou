package com.pinyougou.cart.service;

import java.util.List;

import com.pinyougou.pojogroup.Cart;

public interface CartService {

	/**
	 * 添加商品到购物车
	 * @param cartList
	 * @param itemId
	 * @param num
	 * @return
	 */
	public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num);
	
	/**
	 * 从Redis中获取Cart
	 * @param username
	 * @return
	 */
	public List<Cart> findCartListFromRedis(String username);
	
	/**
	 * 将用户的购物车保存到Redis中
	 * @param username
	 * @return
	 */
	public void saveCartListToRedis(String username, List<Cart> cartList);
	
	/**
	 * 合并两个购物车
	 * @param cartList1
	 * @param cartList2
	 */
	public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2);
	
}
