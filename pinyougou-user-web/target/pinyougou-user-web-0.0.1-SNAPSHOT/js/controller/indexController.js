app.controller('indexController', function($scope, loginService){

	//显示登录名
	$scope.showName=function(){
		loginService.showName().success(
			function(response){	
				$scope.loginName=response.loginName;
			}
		);
	}
});