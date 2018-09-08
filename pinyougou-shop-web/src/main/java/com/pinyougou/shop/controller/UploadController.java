package com.pinyougou.shop.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import entity.Result;
import util.FastDFSClient;

@RestController
public class UploadController {

	@Value("${FILE_SERVER_URL}")
	private String FILE_SERVER_URL;
	@RequestMapping("/upload")
	public Result upload(MultipartFile file) {
		if (file != null) {
	
			String originalFilename  = file.getOriginalFilename();
			String extName = originalFilename.substring(file.getOriginalFilename().indexOf(".")+1);
			try {
				util.FastDFSClient client = new FastDFSClient("classpath:config/fdfs_client.conf");
				String returnPath = client.uploadFile(file.getBytes(), extName);
				String url = FILE_SERVER_URL + returnPath;
				return new Result(true, url);
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		}
		return new Result(false, "上传失败！");
	}
}
