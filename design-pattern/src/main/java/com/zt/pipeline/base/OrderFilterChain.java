package com.zt.pipeline.base;

/**
 * Filter Chain interface 相当于PipelineChain，连接每个Filter
 */
public interface OrderFilterChain<T extends OrderContext> {

    /**
     * 链中节点处理
     */
    void handle(T context);


    /**
     * 是否继续下一步Filter
     */
    void fireNext(T context);

}
