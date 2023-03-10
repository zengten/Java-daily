package com.zt.strategy;

/**
 * 策略模式  发送各种消息通知
 * spring 中 HandlerMethodArgumentResolver, HandlerMethodReturnValueHandler, PropertySourceLoader均使用了策略模式
 */
public interface MessageNotifier {


    boolean support(StrategyTypeEnum type);


    void sendMessage();

}
