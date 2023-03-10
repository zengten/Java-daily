package com.zt.pipeline.biz;

import com.zt.pipeline.base.AbstractOrderFilter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SecondOrderFilter extends AbstractOrderFilter<TestOrderContext> {

    @Override
    public void handle(TestOrderContext context) {
        log.info("order filter second step process...");
    }

}
