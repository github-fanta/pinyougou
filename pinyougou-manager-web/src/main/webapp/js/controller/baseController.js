app.controller('baseController', function($scope){
	
	//分页控件配置
	$scope.paginationConf = {
		currentPage: 1,
		totalItems: 10,
		itemsPerPage: 10,
		perPageOptions: [10,20,30,40,50],
		onChange: function(){
			$scope.reloadList();//重新加载
		}
	};
	
	//刷新列表
	$scope.reloadList=function(){
		$scope.search( $scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage );
	}
	
	$scope.selectIds=[];  //用户勾选的ID集合		
	$scope.updateSelection = function($event,id){
		if($event.target.checked){
			//若此时复选框状态为选中状态
			$scope.selectIds.push(id);
		}else{
			//复选框状态为非选中状态
			var index = $scope.selectIds.indexOf(id);
			$scope.selectIds.splice(index, 1); //index:移除的位置   1：移除的个数
		}
	}
	
	//取出字符串中所有的key属性值
	$scope.jsonToString=function(jsonStr, key){
		var resultStr="";
		var json = JSON.parse(jsonStr);
		for(var i=0; i<json.length; i++){
			if(i>0){
				resultStr += ",";
			}
			resultStr += json[i][key];
		}
		
		return resultStr;
	}
});