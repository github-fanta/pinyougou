package com.pinyougou.cart.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;

import entity.Result;
import util.CookieUtil;

@RestController
@RequestMapping("/cart")
public class CartController {

	@Reference(timeout=5000)
	private CartService cartService;
	@Autowired
	private HttpServletRequest request;
	@Autowired
	private HttpServletResponse response;

	/**
	 * 从cookie获取cartlist
	 * @return
	 */
	@RequestMapping("/findCartList")
	public List<Cart> findCartList() {
		
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		System.out.println(username);
		//提取cookie中的购物车列表数据
		String cartListStr = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
		if (cartListStr == null || cartListStr.equals("")) {
			cartListStr = "[]";
		}
		List<Cart> cartListCookie = JSON.parseArray(cartListStr, Cart.class);
		if (username.equals("anonymousUser")) { //未登录默认用户名为 anonymousUser
			return cartListCookie;
		}else { //已经登录,从Redis中提取
			List<Cart> cartList = cartService.findCartListFromRedis(username);
			if (cartListCookie.size() > 0) {
				cartList = cartService.mergeCartList(cartList, cartListCookie);
				CookieUtil.deleteCookie(request, response, "cartList");	//删除本地cookie中的购物车
				cartService.saveCartListToRedis(username, cartList);	//覆盖原来redis中的值
			}
			return cartList;
		}
	}
	
	/**
	 * 添加商品到购物车
	 * @param itemId
	 * @param num
	 * @return
	 */
	@RequestMapping("/addGoodsToCartList")
	@CrossOrigin(origins="http://localhost:9105", allowCredentials="true")
	public Result addGoodsToCartList(Long itemId, Integer num) {
		
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		
		try {
			List<Cart> cartList = findCartList(); //获取购物车列表(Cookie或者Redis中)
			cartList = cartService.addGoodsToCartList(cartList, itemId, num);
			
			if (username.equals("anonymousUser")) { //匿名用户写回cookie
				String cartListStr = JSON.toJSONString(cartList);
				CookieUtil.setCookie(request, response, "cartList", cartListStr, 3600*24, "UTF-8");
			}else {//登录用户放入Redis中
				cartService.saveCartListToRedis(username, cartList);
			}
			return new Result(true, "添加购物车成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "添加购物车失败");
		}
	}
}
