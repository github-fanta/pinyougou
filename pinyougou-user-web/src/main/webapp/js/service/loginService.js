//首页控制器
app.service('loginService',function($http){
	//发送验证码
	this.showName=function(){
		return $http.get('login/name.do');
	}
});
