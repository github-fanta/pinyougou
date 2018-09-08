app.controller('contentController',function($scope,contentService){
	
	$scope.contentList=[];
	//加载轮播图内容
	$scope.findByCategoryId=function(categoryId){
		contentService.findByCategoryId(categoryId).success(
			function(response){
				$scope.contentList[1]=response;
			}	
		);
	}
	//跳转搜索页
	$scope.search=function(){
		location.href="http://localhost:9104/search.html#?keywords="+$scope.keywords;
	}
});