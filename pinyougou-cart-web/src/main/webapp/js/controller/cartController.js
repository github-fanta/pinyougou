//购物车控制层
app.controller('cartController',function($scope, cartService){
	
	//查找购物车列表
	$scope.findCartList=function(){
		cartService.findCartList().success(
				function(response){
					$scope.cartList=response;
					$scope.totalValue=cartService.sum($scope.cartList); //计算总价格
				}
		);
	}
	
	//添加购物车
	$scope.addGoodsToCartList=function(itemId, num){
		cartService.addGoodsToCartList(itemId, num).success(
				function(response){
					if(response.success){
						$scope.findCartList();  //获取cartList的新cookie
					}else{
						alert(response.message);
					}
				}
		);
	}
	
	//获取用户收货地址列表
	$scope.findAddressList=function(){
		cartService.findListByLoginUser().success(
			function(response){
				$scope.addressList = response;
				//设置默认地址
				for(var i=0; i<$scope.addressList.length; i++){
					if($scope.addressList[i].isDefault == '1'){
						$scope.selectedAddress = $scope.addressList[i];
						break;
					}
				}
			}	
		);
	}
	
	$scope.order={paymentType:'1'}; //默认微信支付
	//选择地址信息
	$scope.selectAddress=function(address){
		$scope.selectedAddress = address;	
	}
	//检测某个地址是否被选中
	$scope.isSelected=function(address){
		return address == $scope.selectedAddress ? true : false;
	}
	
	//选择付款方式
	$scope.selectPayType=function(payType){
		$scope.order.paymentType=payType;
	}
	
	//提交订单
	$scope.submitOrder=function(){
		
		$scope.order.receiverAreaName = $scope.selectedAddress.address;
		$scope.order.receiveMobile = $scope.selectedAddress.mobile;
		$scope.order.receiver = $scope.selectedAddress.contact;
		
		cartService.submitOrder($scope.order).success(
				function(response){
					if(response.success){
						//页面跳转
						//如果是微信支付，跳转到支付页面； 如果货到付款跳转到提示页面
						location.href= $scope.order.paymentType == '1' ? "pay.html" : "paysuccess.html";
					}else{
						alert(response.message);
					}
				}
		);
	}
	
});