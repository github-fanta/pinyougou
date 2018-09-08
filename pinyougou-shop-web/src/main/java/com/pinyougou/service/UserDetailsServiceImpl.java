package com.pinyougou.service;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;

public class UserDetailsServiceImpl implements org.springframework.security.core.userdetails.UserDetailsService {

	private SellerService sellerService;
	
	public void setSellerService(SellerService sellerService) {
		this.sellerService = sellerService;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
	
		Collection<GrantedAuthority> roles = new ArrayList();
		roles.add(new SimpleGrantedAuthority("ROLE_SELLER"));
		
		TbSeller user = sellerService.findOne(username);
		if (user != null && user.getStatus().equals("1")) {   //注册且已经通过审核
			return new User(username, user.getPassword(), roles);
		}
		return null;
	}

}
