package com.zt.pipeline.base;

/**
 * Filter interface  只负责业务处理
 */
public interface OrderFilter<T extends OrderContext> {

    /**
     * 业务处理
     */
    void doFilter(T context, OrderFilterChain chain);

}
