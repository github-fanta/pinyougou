app.controller('userController', function($scope, userService){
	
	//注册
	$scope.reg=function(){
		
		if($scope.entity.password != $scope.checkPassword){
			alert("两次输入密码不一致");
			$scope.entity.password="";
			$scope.checkPassword="";
			return;
		}
		userService.add($scope.entity, $scope.smscode).success(
				function(response){
					alert(response.message);
				}
		);
	}
	
	//发送短信验证码
	$scope.sendCode=function(){
		if($scope.entity.phone == null || $scope.entity.phone == ""){
			alert("请输入手机号");
			return ;
		}
		userService.sendCode($scope.entity.phone).success(
			function(response){
				alert(response.message);
			}
		);
	}
	
	
});