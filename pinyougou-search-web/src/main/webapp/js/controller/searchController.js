app.controller('searchController',function($scope, $location, searchService){
	
	
	$scope.searchMap={'keywords':'','category':'','brand':'','spec':{}, 'price':'', 'pageNo':1, 'pageSize':10, 'sort':'', 'sortField':''};//搜索对象
	$scope.search=function(){
		$scope.searchMap.pageNo= parseInt($scope.searchMap.pageNo) ;
		searchService.search($scope.searchMap).success(
			function(response){
				$scope.resultMap = response;
				buildPageLabel();
			}
		);
	}
	
	buildPageLabel=function(){
		//构建分页栏
		$scope.pageLabel=[];
		var firstPage=1;
		var lastPage=$scope.resultMap.totalPages;
		$scope.firstDot=true;//前面有点
		$scope.lastDot=true;//后边有点

		if($scope.resultMap.totalPages > 5){ //如果页码数大于5
			//只有这两种情况页码窗口是固定的
			if($scope.searchMap.pageNo <= 3){ //如果当前页大于3
				lastPage=5;
				$scope.firstDot=false;//前边没点
			}else if($scope.searchMap.pageNo >= $scope.resultMap.totalPages-2){ //显示后5页
				firstPage=$scope.resultMap.totalPages-4;
				$scope.lastDot=false;//后边没点
			}else{//显示当前页为中心的5页
				firstPage= $scope.searchMap.pageNo - 2;
				lastPage= $scope.searchMap.pageNo + 2;
			}
		}else{
			$scope.firstDot=false;//前面无点
			$scope.lastDot=false;//后边无点
		}
		//将5页装入数组
		for(var pageNo=firstPage; pageNo<= lastPage; pageNo++){
			$scope.pageLabel.push(pageNo);
		}
	}
	//添加搜索项
	$scope.addSearchItem=function(key,value){
		if(key=='category' || key=='brand' || key=='price'){//如果点击的是分类或者是品牌
			$scope.searchMap[key]=value;
		}else{
			$scope.searchMap.spec[key]=value;
		}	
		
		$scope.search();
	}

	$scope.removeSearchItem=function(key){
		if(key=='category' || key=='brand' || key=='price'){//分类或品牌
			$scope.searchMap[key]='';
		}else{//规格
			delete $scope.searchMap.spec[key];
		}	
		$scope.search();
	}
	
	//按页查找
	$scope.searchByPage=function(pageNo){
		//defensive Code
		if(pageNo < 1 || pageNo > $scope.resultMap.totalPages){
			return;
		}
		$scope.searchMap.pageNo = pageNo;
		$scope.search();
	}
	
	//判断当前页为第一页
	$scope.isTopPage=function(){
		if($scope.searchMap.pageNo==1){
			return true;
		}else{
			return false;
		}
	}
	
	//判断当前页是否未最后一页
	$scope.isEndPage=function(){
		if($scope.searchMap.pageNo==$scope.resultMap.totalPages){
			return true;
		}else{
			return false;
		}
	}

	//根据字段sortField，排序  sort: DESC  ASC
	$scope.sortSearch=function(sort, sortField){
		$scope.searchMap.sort=sort;
		$scope.searchMap.sortField=sortField;
		$scope.search();
	}

	//判断输入关键字是否包含品牌
	$scope.keywordsIsBrand=function(){
		for(var i=0; i< $scope.resultMap.brandList.length; i++){
			if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text) >= 0){
				return true;
			}
		}
		return false;
	}
	
	//跳转搜索页自动加载搜索
	$scope.loadKeywords=function(){
		$scope.searchMap.keywords = $location.search()['keywords'];
		$scope.search();
	}
});