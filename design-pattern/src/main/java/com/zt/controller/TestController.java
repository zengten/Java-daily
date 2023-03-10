package com.zt.controller;

import com.zt.service.TestService;
import com.zt.strategy.StrategyTypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;

    /**
     * 发送短信通知
     */
    @RequestMapping("/testSmsMessage")
    public void testSmsMessage() {
        testService.sendMessage(StrategyTypeEnum.SMS);
    }

    /**
     * 发送app通知
     */
    @RequestMapping("/testAppMessage")
    public void testAppMessage() {
        testService.sendMessage(StrategyTypeEnum.APP);
    }


    /**
     * 发送app通知
     */
    @RequestMapping("/testPipeline")
    public void testPipeline() {
        testService.pipelineProcess();
    }

}
