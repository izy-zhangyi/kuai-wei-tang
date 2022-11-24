package com.kwt.controller;


import com.kwt.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.UUID;

/**
 * 文件上传与下载
 */
@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${reggie.path}")
    private String path;

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        //1、获取上传的文件名称
        String originalFilename = file.getOriginalFilename();

        //1.2、拿到文件之后， 以“.”进行截取，获取文件后缀名
        String substring = originalFilename.substring(originalFilename.lastIndexOf("."));

        //1.3、使用UUID方式重新生成文件名，防止文件名称因重复而造成的文件覆盖问题
        String filename = UUID.randomUUID().toString();

        //1.4、字符串拼接，获取完整文件名称
        String newFilename = filename + substring;

        //2 将文件写到文件夹中去,创建file对象（文件）
        // ,将路径参数传递过去，表示要在该路径下创建文件夹，存储文件
        File file1 = new File(path);

        try {
            //2.1、判断该路径下是否存在文件夹
            if (!file1.exists()) {
                file1.mkdirs();
            }
            //2.2 将文件存储到该文件夹里面
            file.transferTo(new File(path + newFilename));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //3、一系列操作完成之后，文件也存储到该路径文件夹中
        // ，最后再将储存成功（文件全名称）的消息返回就行
        return R.success(newFilename);
    }

    @GetMapping("/download")
    public void download(String name, HttpServletResponse response){
        //jdk7---->try-while---resource
        //1、文件下载，就是将文件写到本地路径
        //1.1、读取到文件,之后将文件响应输出到该路径下
        try (FileInputStream fis = new FileInputStream(path + name);
             ServletOutputStream outputStream = response.getOutputStream()) {
            //将读取到的文件输出到该路径下
            //1、定义一次输出最大的数组（即：一次性可以写多少字节）
            byte[] bytes = new byte[4096];
            //定义一个常量，接收总共写入多少次字节
            int len;
            while ((len = fis.read(bytes))!=-1){
                outputStream.write(bytes,0,len);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
