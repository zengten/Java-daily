package com.zt.service;

import com.zt.strategy.StrategyTypeEnum;

public interface TestService {

    /**
     * 发送通知
     */
    void sendMessage(StrategyTypeEnum type);



    void pipelineProcess();

}
