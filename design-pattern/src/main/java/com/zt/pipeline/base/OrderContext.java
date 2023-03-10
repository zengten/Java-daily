package com.zt.pipeline.base;

/**
 * Filter 执行上下文
 */
public interface OrderContext {

    /**
     * 获取业务编码
     */
    BizEnum getBizEnum();


    /**
     * 获取Filter选择器
     */
    FilterSelector getFilterSelector();

    /**
     * 是否继续执行
     */
    boolean continueChain();

}
