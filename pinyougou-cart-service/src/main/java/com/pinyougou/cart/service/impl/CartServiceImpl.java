package com.pinyougou.cart.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;

@Service
public class CartServiceImpl implements CartService{

	@Autowired
	private TbItemMapper itemMapper;
	/**
	 * 添加货物到购物车
	 */
	@Override
	public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
		
		//根据itemId查找SKU TbItem
		TbItem item = itemMapper.selectByPrimaryKey(itemId);
		if (item == null) {
			throw new RuntimeException("无此商品");
		}
		if (!item.getStatus().equals("1")) {
			throw new RuntimeException("商品状态无效");
		}
		//根据TbItem对象找到sellerId
		String sellerId = item.getSellerId();
		//根据sellerId找到这个商家的购物车
		Cart cart = searchCartBySellerId(cartList, sellerId);
			//如果没有，则建立一个新的购物车,将订单项加入购物车
		if (cart == null) {
			cart = new Cart();
			cart.setSellerId(sellerId);
			cart.setSellerName(item.getSeller());
			//创建订单项
			TbOrderItem orderItem = createOrderItem(item, num);
			List<TbOrderItem> orderItemList = new ArrayList<>();
			orderItemList.add(orderItem);
			cart.setOrderItemList(orderItemList);
			cartList.add(cart);
		}else {
			//如果有购物车
				//查找这个购物车中有没有这个商品（itemId）
			TbOrderItem orderItem = searchOrderItemByItemId(cart, itemId);
					//如果没有，新建一个订单项目，加入购物车中
			if (orderItem == null) {
				TbOrderItem searchOrderItem = createOrderItem(item, num);
				cart.getOrderItemList().add(searchOrderItem);
			}else {
				//如果有，将原商品数量加num
				orderItem.setNum(orderItem.getNum()+num);
				orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum()));
				//添加完成检查购物车
				if (orderItem.getNum() < 1) {  //数目小于1
					cart.getOrderItemList().remove(orderItem);//移除此订单项
				}
				//如果此购物车为空，从购物车列表移除此购物车
				if (cart.getOrderItemList().size() == 0) {
					cartList.remove(cart);
				}
			}
		}	
		return cartList;
	}
	
	//根据itemId查找购物车中某一订单项
	private TbOrderItem searchOrderItemByItemId(Cart cart, Long itemId) {
		for (TbOrderItem orderItem : cart.getOrderItemList()) {
			if (orderItem.getItemId().longValue() == itemId.longValue()) {
				return orderItem;
			}
		}
		return null;
	}

	//创建一个订单项
	private TbOrderItem createOrderItem(TbItem item, int num) {
		if (num < 1) {
			throw new RuntimeException("数目非法！");
		}
		TbOrderItem orderItem = new TbOrderItem();
		orderItem.setGoodsId(item.getGoodsId());
		orderItem.setItemId(item.getId());
		orderItem.setNum(num);
		orderItem.setPicPath(item.getImage());
		orderItem.setPrice(item.getPrice());
		orderItem.setSellerId(item.getSellerId());
		orderItem.setTitle(item.getTitle());
		orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
		return orderItem;
	}

	//查找商家购物车
	private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
		for (Cart cart : cartList) {
			if (cart.getSellerId().equals(sellerId)) {
				return cart;
			}
		}
		return null;
	}

	
	@Autowired
	private RedisTemplate redisTemplate;
	/**
	 * 从Redis中查找用户购物车
	 */
	@Override
	public List<Cart> findCartListFromRedis(String username) {
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
		return cartList == null ? new ArrayList<Cart>() : cartList;
	}

	/**
	 * 保存用户购物车到Redis
	 */
	@Override
	public void saveCartListToRedis(String username, List<Cart> cartList) {
		redisTemplate.boundHashOps("cartList").put(username, cartList);
	}

	/**
	 * 合并两个购物车
	 */
	@Override
	public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
		
		System.out.println("合并购物车");
		for (Cart cart : cartList2) {
			List<TbOrderItem> itemList = cart.getOrderItemList();
			for (TbOrderItem orderItem : itemList) {
				cartList1 = addGoodsToCartList(cartList1, orderItem.getItemId(), orderItem.getNum());
			}
		}
		return cartList1;
	}

}
