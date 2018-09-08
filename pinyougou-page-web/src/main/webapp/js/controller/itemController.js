 //控制层 
app.controller('itemController' ,function($scope,$http){	
	$scope.num = 1;
	$scope.addNum=function(x){
		$scope.num += x;
		if($scope.num < 1){
			$scope.num = 1;
		}
	}
	//用户选择规格属性
	$scope.specificationItems={};
	$scope.selectSpecification=function(name, value){
		$scope.specificationItems[name]=value;
		$scope.searchSku();//读取sku
	}
	
	//判断某规格选项是否被用户选中
	$scope.isSelected=function(name,value){
		return ($scope.specificationItems[name] == value)?true:false;
	}
	
	//用户选择sku信息
	$scope.sku={};
	//加载默认sku
	$scope.loadSku=function(){
		$scope.sku = skuList[0];
		$scope.specificationItems = JSON.parse(JSON.stringify($scope.sku.spec));
	}
	
	//查询SKU
	$scope.searchSku=function(){
		for(var i=0; i<skuList.length; i++){
			//若用户选择的匹配到了sku列表中的某个规格列表，把这个sku列表中的某一个sku取出显示
			if($scope.matchObject($scope.specificationItems,skuList[i].spec)){
				$scope.sku = skuList[i];
				return;
			}
		}
		$scope.sku={id:0,title:'--------',price:0};//如果没有匹配的	
	}
	
	//匹配两个对象
	$scope.matchObject=function(map1, map2){
		for(var k in map1){
			if(map1[k] != map2[k]){
				return false;
			}
		}
		
		for(var k in map2){
			if(map2[k] != map1[k]){
				return false;
			}
		}
		return true;
	}
	
	//添加商品到购物车
	$scope.addToCart=function(){
		$http.get('http://localhost:9107/cart/addGoodsToCartList.do?itemId='
				+$scope.sku.id+'&num='+$scope.num, {'withCredentials':true}).success(
				function(response){
					if(response.success){
						location.href='http://localhost:9107/cart.html';//跳转到购物车页面
					}else{
						alert(response.message);
					}
				}
		);
	}
});	
