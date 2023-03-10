package com.zt.pipeline.base;

public abstract class AbstractOrderContext implements OrderContext {

    private BizEnum bizEnum;

    private FilterSelector filterSelector;

    public AbstractOrderContext(BizEnum bizEnum, FilterSelector filterSelector) {
        this.bizEnum = bizEnum;
        this.filterSelector = filterSelector;
    }

    @Override
    public BizEnum getBizEnum() {
        return bizEnum;
    }

    @Override
    public FilterSelector getFilterSelector() {
        return filterSelector;
    }
}
