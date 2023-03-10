package com.zt.pipeline.biz;

import com.zt.pipeline.base.AbstractOrderContext;
import com.zt.pipeline.base.BizEnum;
import com.zt.pipeline.base.FilterSelector;
import lombok.Getter;
import lombok.Setter;

/**
 * 具体context实现，可添加param，和result对象
 */
public class TestOrderContext extends AbstractOrderContext {

    private boolean continueFlag;

    @Getter
    @Setter
    private Integer value;

    @Getter
    @Setter
    private String msg;

    public TestOrderContext(BizEnum bizEnum, FilterSelector filterSelector) {
        super(bizEnum, filterSelector);
        continueFlag = true;
    }

    @Override
    public boolean continueChain() {
        return continueFlag;
    }

}
