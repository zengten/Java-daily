
package com.zt;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;


/**
 * @author ZT
 */
@MapperScan(basePackages = "com.zt.dao")
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.zt"})
public class Bank2Server {
	
	public static void main(String[] args) {
		SpringApplication.run(Bank2Server.class, args);
	}

}
