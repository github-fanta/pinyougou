app.controller("brandController", function($scope,$controller,brandService){
	
	//继承baseController
	$controller('baseController', {$scope:$scope});
	
			//查询品牌列表
			$scope.findAll=function(){		
				brandService.findAll().success(
					function(response){
						alert(response.size());
						$scope.list = response;
					}	
				);
			}
				
			//分页查找
			$scope.findPage=function(pageNum,pageSize){
				brandService.findPage(pageNum,pageSize).success(
					function(response){
						$scope.list = response.rows;
						$scope.paginationConf.totalItems=response.total;	//更新总记录数
					}
				);
			}
			//新增
			$scope.save=function(){
				var serviceMethod = null;
				if($scope.entity.id != null){
					serviceMethod = brandService.update($scope.entity);
				}else{
					serviceMethod = brandService.add($scope.entity);
				}
				
				serviceMethod.success(
						function(response){
							if(response.success == true){
								$scope.reloadList();
							}else{
								alert(response.message);
							}
						}
				);
			}
			
			//查找
			$scope.findOne=function(id){
				brandService.findOne(id).success(
					function(response){
						$scope.entity = response;
					}		
				);
			}
			
			
			//删除
			$scope.delete=function(){
				if(confirm("确定要删除吗？")){
					brandService.delete($scope.selectIds).success(
						function(response){
							if(response.success){
								$scope.reloadList();
							}else{
								alert(response.message);
							}
						}
					);
				}
			}		
			
			//查找
			$scope.searchEntity = {};
			$scope.search = function(pageNum, pageSize){
				brandService.search(pageNum, pageSize, $scope.searchEntity).success(
						function(response){
							$scope.list = response.rows;  //显示当前页数据
							$scope.paginationConf.totalItems = response.total;  //更新总记录数
						}
				);
			}
			
});