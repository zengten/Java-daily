package com.zt.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AppMessageNotifier implements MessageNotifier {

    @Override
    public boolean support(StrategyTypeEnum type) {
        return StrategyTypeEnum.APP.equals(type);
    }

    @Override
    public void sendMessage() {
        log.info("发送app通知消息");
    }

}
