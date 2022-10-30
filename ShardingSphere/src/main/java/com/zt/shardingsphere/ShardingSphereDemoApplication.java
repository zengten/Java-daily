package com.zt.shardingsphere;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author ZT
 */
@MapperScan("com.zt.shardingsphere.dao")
@SpringBootApplication
public class ShardingSphereDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShardingSphereDemoApplication.class, args);
    }

}