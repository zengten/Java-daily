package com.zt.service;


import com.zt.pipeline.base.BizEnum;
import com.zt.pipeline.base.FilterChainPipeline;
import com.zt.pipeline.base.LocalListFilterSelector;
import com.zt.pipeline.biz.FirstOrderFilter;
import com.zt.pipeline.biz.SecondOrderFilter;
import com.zt.pipeline.biz.TestOrderContext;
import com.zt.pipeline.biz.ThirdOrderFilter;
import com.zt.strategy.MessageNotifier;
import com.zt.strategy.StrategyTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final List<MessageNotifier> messageNotifierList;


    private final FilterChainPipeline filterChainPipeline;


    @Override
    public void sendMessage(StrategyTypeEnum type) {
        for (MessageNotifier messageNotifier : messageNotifierList) {
            if(messageNotifier.support(type)) {
                messageNotifier.sendMessage();
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void pipelineProcess() {
        // selector 可采用数据库配置
        LocalListFilterSelector filterSelector = new LocalListFilterSelector();
        filterSelector.addFilter(FirstOrderFilter.class.getSimpleName());
        filterSelector.addFilter(SecondOrderFilter.class.getSimpleName());
        filterSelector.addFilter(ThirdOrderFilter.class.getSimpleName());
        // context设置param和result
        TestOrderContext context = new TestOrderContext(BizEnum.BIZ_XXX, filterSelector);
        filterChainPipeline.getFilterChain().handle(context);
        String msg = context.getMsg();
        log.info("msg = {}", msg);
        Integer value = context.getValue();
        log.info("value = {}", value);
    }
}
