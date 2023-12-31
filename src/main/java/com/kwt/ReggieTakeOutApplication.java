package com.kwt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@Slf4j
@ServletComponentScan //开启 servlet 组件扫描，扫描 WebFilter 过滤器
@EnableTransactionManagement //开启事务管理器
@EnableCaching //开启 spring cache 的注解缓存的功能
public class ReggieTakeOutApplication {

    public static void main(String[] args) {
        log.info("项目开始启动了......");
        SpringApplication.run(ReggieTakeOutApplication.class, args);
        log.info("项目启动成功......");
    }

}