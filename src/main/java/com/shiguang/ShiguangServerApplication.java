package com.shiguang;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 「拾光」服务端启动入口。
 *
 * @MapperScan 统一扫描 mapper 接口，接口上无需再逐个标注 @Mapper。
 */
@SpringBootApplication
@MapperScan("com.shiguang.mapper")
public class ShiguangServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShiguangServerApplication.class, args);
    }
}
