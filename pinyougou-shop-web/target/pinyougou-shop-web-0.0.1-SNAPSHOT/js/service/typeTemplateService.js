//服务层
app.service('typeTemplateService',function($http){
	    	
	//读取列表数据绑定到表单中
	this.findAll=function(){
		return $http.get('../typeTemplate/findAll.do');		
	}
	//分页 
	this.findPage=function(page,rows){
		return $http.get('../typeTemplate/findPage.do?page='+page+'&rows='+rows);
	}
	//查询实体
	this.findOne=function(id){
		return $http.get('../typeTemplate/findOne.do?id='+id);
	}
	//增加 
	this.add=function(entity){
		return  $http.post('../typeTemplate/add.do',entity );
	}
	//修改 
	this.update=function(entity){
		return  $http.post('../typeTemplate/update.do',entity );
	}
	//删除
	this.dele=function(ids){
		return $http.get('../typeTemplate/delete.do?ids='+ids);
	}
	//搜索
	this.search=function(page,rows,searchEntity){
		return $http.post('../typeTemplate/search.do?page='+page+"&rows="+rows, searchEntity);
	}  
	
	//保存
	this.save=function(entity){
		return $http.post('../typeTemplate/add.do', entity);
	}
	
	
	//查询下拉列表类型模板
	this.selectOptionList = function(){
		return $http.get('../typeTemplate/selectOptionList.do');
	}

	//查询规格列表和其表项的组合
	this.findSpecList=function(typeId){
		return $http.get('../typeTemplate/findSpecList.do?typeId='+typeId);
	}
});
