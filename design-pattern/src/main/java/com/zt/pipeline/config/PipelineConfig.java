package com.zt.pipeline.config;


import com.zt.pipeline.base.FilterChainPipeline;
import com.zt.pipeline.base.OrderFilter;
import com.zt.pipeline.biz.FirstOrderFilter;
import com.zt.pipeline.biz.SecondOrderFilter;
import com.zt.pipeline.biz.ThirdOrderFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * pipeline 配置类
 */
@Configuration
public class PipelineConfig {


    @Bean
    @SuppressWarnings("unchecked")
    public FilterChainPipeline filterChainPipeline() {
        FilterChainPipeline<OrderFilter> pipeline = new FilterChainPipeline<>();
        pipeline.addFilterChain(new ThirdOrderFilter())
                .addFilterChain(new SecondOrderFilter())
                .addFilterChain(new FirstOrderFilter());
        return pipeline;
    }

}
