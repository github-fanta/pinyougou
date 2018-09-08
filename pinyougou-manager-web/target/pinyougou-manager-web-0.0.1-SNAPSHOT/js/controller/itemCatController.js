 //控制层 
app.controller('itemCatController' ,function($scope,$controller   ,itemCatService){	
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		itemCatService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		itemCatService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		itemCatService.findOne(id).success(
			function(response){
				$scope.entity= response;//只有typeId 没有typeName  	
				//拿到所有类型模板的json数据
				var typeTemplateOptionData = $scope.typeTemplateOptionList['data']; //$scope.typeTemplateOptionList = {data:[]};
				//找到类型模板中id为typeId的typeName
				for(var i=0; i<typeTemplateOptionData.length; i++){
					if(response.typeId == typeTemplateOptionData[i]['id']){
						//拼凑json放入与select2绑定的变量selectedJson中
						var str = "{\"id\":\""+response.typeId+"\", \"text\":\""+typeTemplateOptionData[i]['text']+"\"}";
						$scope.selectedJson =  JSON.parse(str);
						break;
					}
				}
			}
		);				
	}
	
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			$scope.entity.typeId = $scope.selectedJson['id'];
			serviceObject=itemCatService.update( $scope.entity ); //修改  
		}else{
			$scope.entity.parentId = $scope.parentId;
			serviceObject=itemCatService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
					$scope.findByParentId($scope.parentId);//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){	
		if(confirm("确定删除吗？")){
			//获取选中的复选框			
			itemCatService.dele( $scope.selectIds ).success(
				function(response){
					if(response.success){
						$scope.findByParentId($scope.parentId);//刷新列表
						$scope.selectIds=[];
					}						
				}		
			);	
		}
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		itemCatService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	$scope.parentId=0;
	//根据父节点信息查找分类
	$scope.findByParentId=function(parentId){
		$scope.parentId=parentId;//保存当前父节点
		itemCatService.findByParentId(parentId).success(
			function(response){
				$scope.list=response;
			}
		);
	}
	
	$scope.grade=1;
	$scope.selectList=function(grade, entity){
		$scope.grade = grade;
		if(grade == 1){
			$scope.entity_1 = null;
			$scope.entity_2 = null;
		}
		if(grade == 2){
			$scope.entity_1 = entity;
			$scope.entity_2 = null;	
		}
		if(grade == 3){
			$scope.entity_2 = entity;
		}
		
		$scope.findByParentId(entity.id);
	}

	
	$scope.typeTemplateOptionList = {data:[]};
	//查询下拉列表类型模板
	$scope.selectOptionList = function(){
		itemCatService.selectOptionList().success(
			function(response){
				$scope.typeTemplateOptionList={data:response};
			}
		);
	}
});	
