package com.zt.spring;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author ZT
 */
@FeignClient(value = "seata-demo1-bank2", fallback = Bank2ClientFallback.class)
public interface Bank2Client {
    /**
     * 远程调用李四的微服务
     *
     * @param amount
     * @return
     */
    @GetMapping("/bank2/transfer")
    String transfer(@RequestParam("amount") Double amount);
}
