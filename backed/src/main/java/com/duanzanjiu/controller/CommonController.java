package com.duanzanjiu.controller;

import com.duanzanjiu.common.Result;
import com.duanzanjiu.common.ResultCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/common")


public class CommonController {
    @Value("${file.path-path}")
    private String uploadPath;
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam MultipartFile file) throws IOException {
        //检查文件是否为空
        if (file.isEmpty()) {
            return Result.error(ResultCode.SYSTEM_ERROR.getCode(), "文件为空", null);
        }
        //获取文件的相关信息
        String fileName = file.getOriginalFilename();
        String fileSuffix = fileName != null ? fileName.substring(fileName.lastIndexOf(".")) :"png";
        String fName = UUID.randomUUID().toString().replace("-", "") + fileSuffix;
        //创建文件路径
        File dir =new File(uploadPath);
        if(!dir.exists()){
            dir.mkdirs();
        }
        file.transferTo(new File(dir.getAbsoluteFile() + File.separator + fName));
        String url = "/uploads/" + fName;
        return Result.ok(url);



    }
}
