 //控制层 
app.controller('goodsController' ,function($scope,$controller   ,$location,goodsService,uploadService,itemCatService,typeTemplateService){	
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){
		var id = $location.search()['id'];
		if(id == null){
			return;
		}
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;
				editor.html(response.goodsDesc.introduction);//副文本框设置商品描述
				$scope.entity.goodsDesc.itemImages=JSON.parse($scope.entity.goodsDesc.itemImages);//转化图片json数据
				$scope.entity.goodsDesc.customAttributeItems=JSON.parse($scope.entity.goodsDesc.customAttributeItems);//转换用户自定义属性字符串为json数据
				//规格				
				$scope.entity.goodsDesc.specificationItems=JSON.parse($scope.entity.goodsDesc.specificationItems);	
				//$scope.entity.itemList = JSON.parse($scope.entity.itemList); //规格列表
				//SKU列表规格列转换				
				for( var i=0;i<$scope.entity.itemList.length;i++ ){
					$scope.entity.itemList[i].spec = 
					JSON.parse( $scope.entity.itemList[i].spec);		
				}			

			}
		);				
	}
	
	
	//保存 
	$scope.save=function(){	
		
		$scope.entity.goodsDesc.introduction = editor.html();
		var serviceObject;//服务层对象  				
		if($scope.entity.goods.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					alert("新增成功");
		        	location.href="goods.html";
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	//保存 
	$scope.add=function(){	
	
		$scope.entity.goodsDesc.introduction = editor.html();
		goodsService.add( $scope.entity  ).success(
			function(response){
				if(response.success){
					alert("新增成功");
		        	$scope.entity={};//清空数据
		        	editor.html("");
				}else{
					alert(response.message);
				}
			}		
		);
	}
	
	
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	$scope.image_entity = {};
	$scope.uploadFile=function(){
		uploadService.uploadService().success(
			function(response){
				if(response.success){
					$scope.image_entity.url = response.message;
				}else{
					alert(response.message);
				}
			}	
		);
	}
	
	$scope.entity={goodsDesc:{itemImages:[], specificationItems:[]}};
	$scope.add_image_entity=function(){
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
	}
	$scope.remove_image_entity=function(index){
		$scope.entity.goodsDesc.itemImages.splice(index,1);
	}
    
	
	//查找一级分类
	$scope.selectItemCatList1 = function(){
		itemCatService.findByParentId(0).success(
			function(response){
				$scope.itemCatList1=response;
			}	
		);
	}
	
	//查找二级分类
	$scope.$watch('entity.goods.category1Id',function(newValue, oldValue){
		itemCatService.findByParentId(newValue).success(
				function(response){
					$scope.itemCatList2=response;
				}
		);
	});
	//查找三级分类
	$scope.$watch('entity.goods.category2Id',function(newValue, oldValue){
		itemCatService.findByParentId(newValue).success(
				function(response){
					$scope.itemCatList3=response;
				}
		);
	});
	
	//查找类型模板ID
	$scope.$watch('entity.goods.category3Id',function(newValue, oldValue){
		itemCatService.findOne(newValue).success(
			function(response){
				$scope.entity.goods.typeTemplateId=response.typeId;
			}	
		);
	});
	
	//监控ID查找类型模板
	$scope.$watch('entity.goods.typeTemplateId',function(newValue,oldValue){
		
		typeTemplateService.findOne(newValue).success(
			function(response){
				$scope.brandList = JSON.parse(response.brandIds);
				if($scope.entity.goods.id == null){ //仅当新增商品时查找
					$scope.entity.goodsDesc.customAttributeItems = JSON.parse(response.customAttributeItems);
				}
			}	
		);
		
		//查询规格项
		typeTemplateService.findSpecList(newValue).success(
			function(response){
				 $scope.specList=response;
			}	
		);
	});
	
	//更新规格选项数据
	$scope.updateSpecAttribute=function($event,name,value){
		//查找更新的是哪个规格的json
		var spec = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,"attributeName",name);
		if(spec == null){//还没有添加此规格属性json数据
			//拼凑规格json数据添加到specificationItems数组中
			//$scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems);
			$scope.entity.goodsDesc.specificationItems.push({"attributeName":name,"attributeValue":[value]});
		}else{//此规格属性json已经存在
			//添加到此规格属性的attributeValue数组中
			if($event.target.checked){
				spec['attributeValue'].push(value);
			}else{
				spec['attributeValue'].splice(spec['attributeValue'].indexOf(value), 1);
				if(spec['attributeValue'].length == 0){
					$scope.entity.goodsDesc.specificationItems.splice( $scope.entity.goodsDesc.specificationItems.indexOf(spec) ,1);
				}
			}
		}	
	}
	
	$scope.createItemList=function(){
		$scope.entity.itemList = [{spec:{},price:0,num:9999,status:'0', isDefault:'0'}];//初始
		var specItems = $scope.entity.goodsDesc.specificationItems;	//对于选中的规格中的所有规格项目
		for(var i=0; i<specItems.length; i++){ //把每个规格添加进表中
			$scope.entity.itemList=addColumn($scope.entity.itemList, specItems[i].attributeName, specItems[i].attributeValue);
		}
	}
	
	addColumn=function(itemList, attributeName, attributeValueArr){
		var newList=[];
		//对之前的itemList每一行添加进所有属性
		for(var i=0; i<itemList.length; i++){
			var oldRow = itemList[i];
			//添加此此规格的所有选中的规格项
			for(var j=0; j<attributeValueArr.length; j++){
				newRow = JSON.parse(JSON.stringify(oldRow));
				newRow.spec[attributeName]=attributeValueArr[j];
				newList.push(newRow);
			}
		}
		return newList;
	}
	
	$scope.statusArr=['未审核','已审核','审核未通过','关闭'];
	
	$scope.itemCatArr=[];
	$scope.findItemCatArr=function(){
		itemCatService.findAll().success(
			function(response){
				for(var i=0; i<response.length; i++){
					$scope.itemCatArr[response[i].id] = response[i].name;
				}
			}	
		);
	}
	
	//查找某个商品的规格中specName规格的规格项数组中是否含有叫optionName的规格项，有返回true,没有返回false
	//返回商品的规格项specificationItems = [{"attributeName":"网络制式","attributeValue":["移动4G"]},{"attributeName":"屏幕尺寸","attributeValue":["5.5寸","4.5寸"]}]
	$scope.checkAttributeValue = function(specName, optionName){ //如 找"网络制式"(specName)中有没有3G(optionName)
		
		var specItems = $scope.entity.goodsDesc.specificationItems;//此商品的所有规格项  如[{"attributeName":"网络制式","attributeValue":["移动4G"]},{"attributeName":"屏幕尺寸","attributeValue":["5.5寸","4.5寸"]}]
		
		var spec = $scope.searchObjectByKey(specItems,'attributeName',specName); 
		//检查类型模板的所有规格中，这个商品是否有此规格
		//此商品有叫specName的规格且规格项数组中有一个叫optionName的规格项
			if(spec != null && spec.attributeValue.indexOf(optionName) >= 0){
				//规格项数组（attributeValue）中有一个叫optionName的规格项
				return true;
			}else{
				return false
			}
	}
});	
