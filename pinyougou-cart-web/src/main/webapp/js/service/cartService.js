//购物车服务层
app.service('cartService', function($http){
	
	//查询购物车
	this.findCartList=function(){
		return $http.get('cart/findCartList.do');
	}
	//添加到购物车
	this.addGoodsToCartList=function(itemId, num){
		return $http.post('cart/addGoodsToCartList.do?itemId='+itemId+'&num='+num);
	}
	
	//计算所有购物车总货物数量和总价格
	this.sum=function(cartList){
		var totalValue={'totalNum':0, 'totalMoney':0.00};
		for(var i=0; i<cartList.length; i++){ //第几个购物车
			var cart = cartList[i];
			for(var j=0; j<cart.orderItemList.length; j++){ //本购物车中第几个订单项
				var orderItem = cart.orderItemList[j];
				totalValue.totalNum += orderItem.num;
				totalValue.totalMoney += orderItem.totalFee;
			}
		}
		return totalValue;
	}
	
	//查找用户收货地址列表
	this.findListByLoginUser=function(){
		return $http.get('address/findListByLoginUser.do');
	}
	//提交订单
	this.submitOrder=function(order){
		return $http.post('order/add.do', order);
	}
});