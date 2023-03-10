package com.zt.pipeline.base;

/**
 * Filter Chain 默认实现
 */
@SuppressWarnings("unchecked")
public class DeFaultFilterChain<T extends OrderContext> implements OrderFilterChain<T> {

    /**
     * 装饰 OrderFilterChain
     */
    private OrderFilterChain<T> nextChain;

    private OrderFilter<T> filter;

    DeFaultFilterChain(OrderFilterChain nextChain, OrderFilter filter) {
        this.nextChain = nextChain;
        this.filter = filter;
    }

    @Override
    public void handle(T context) {
        filter.doFilter(context, this);
    }

    @Override
    public void fireNext(T context) {
        OrderFilterChain next = this.nextChain;
        if (next != null) {
            next.handle(context);
        }
    }

}
