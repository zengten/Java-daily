package com.zt.pipeline.biz;

import com.zt.pipeline.base.AbstractOrderFilter;
import com.zt.pipeline.base.BizEnum;
import com.zt.pipeline.base.FilterSelector;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 依赖的业务service 可统一使用门面 FacadeService 获取
 */
@Slf4j
public class FirstOrderFilter extends AbstractOrderFilter<TestOrderContext> {

    @Override
    public void handle(TestOrderContext context) {
        BizEnum bizEnum = context.getBizEnum();
        FilterSelector filterSelector = context.getFilterSelector();
        List<String> filterNames = filterSelector.getFilterNames();
        log.info("order filter first step process...{}..{}..{}",
                bizEnum.getName(),
                filterSelector.getClass().getSimpleName(),
                filterNames);
        context.setValue(1);
        context.setMsg("first exec");
    }

}
