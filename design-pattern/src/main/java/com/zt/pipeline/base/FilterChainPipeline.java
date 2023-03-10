package com.zt.pipeline.base;

/**
 * 组成pipeline链 各个节点
 */
public class FilterChainPipeline<T extends OrderFilter> {

    private OrderFilterChain last;

    public OrderFilterChain getFilterChain() {
        return last;
    }

    public FilterChainPipeline addFilterChain(T orderFilter){
        this.last = new DeFaultFilterChain(this.last, orderFilter);
        return this;
    }
}
