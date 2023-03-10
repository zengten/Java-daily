package com.zt.pipeline.base;

/**
 * Filter业务抽象类
 */
public abstract class AbstractOrderFilter<T extends OrderContext> implements OrderFilter<T> {

    @Override
    @SuppressWarnings("unchecked")
    public void doFilter(T context, OrderFilterChain chain) {
        // 根据selector确定是否执行当前过滤器
        if (context.getFilterSelector().matchFilter(this.getClass().getSimpleName())) {
            handle(context);
        }
        if (context.continueChain()) {
            chain.fireNext(context);
        }
    }

    protected abstract void handle(T context);

}
