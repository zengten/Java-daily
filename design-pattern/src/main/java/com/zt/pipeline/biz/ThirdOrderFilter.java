package com.zt.pipeline.biz;

import com.zt.pipeline.base.AbstractOrderFilter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ThirdOrderFilter extends AbstractOrderFilter<TestOrderContext> {

    @Override
    public void handle(TestOrderContext context) {
        log.info("order filter third step process...");
    }

}
