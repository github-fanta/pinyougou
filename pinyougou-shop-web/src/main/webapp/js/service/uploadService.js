app.service('uploadService', function($http){
	
	this.uploadService=function(){
		var formData = new FormData();
		formData.append("file", file.files[0]);
		return $http({
			method:'POST',
			url:'../upload.do',
			data:formData,
			headers:{'Content-Type':undefined},//现在是multipart类型，若不定义的话默认是json格式
			transformRequest:angular.identity  //angular提供对表单进行二进制序列化
		});
	}
});