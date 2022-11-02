
package com.zt;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;


/**
 * seata 分布式事务
 * 1.每个微服务先必须创建 undo_log 表，用于分布式事务提交和回滚
 * 2.安装事务协调器，seata-server: https://github.com/seata/seata/releases
 *      并且配置seata-server的配置中心，如nacos地址，并启动
 * 3.所有想要用到分布式事务的微服务必须使用 seata DataSourceProxy 代理数据源
 * 4.每个微服务都必须导入配置文件 register.conf 和 file.conf，并配置
 *      配置参考地址：https://github.com/seata/seata-samples/blob/master/doc/quick-integration-with-spring-cloud.md
 * 5.给每个分布式大事务方法标注 @GlobalTransactional 注解代表全局事务
 * 6.每个远程调用的小事务方法使用 @Transactional 标注
 * @author ZT
 */
@MapperScan(basePackages = "com.zt.dao")
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.zt.spring"})
public class Bank1Server {
	
	public static void main(String[] args) {
		SpringApplication.run(Bank1Server.class, args);
	}

}
