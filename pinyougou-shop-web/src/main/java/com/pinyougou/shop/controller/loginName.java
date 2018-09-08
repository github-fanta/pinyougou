package com.pinyougou.shop.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller")
public class loginName {

	@RequestMapping("/name.do")
	public Map<String, String> name(){
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		Map<String, String> map = new HashMap<String, String>();
		map.put("loginName", name);
		return map;
	}
}
