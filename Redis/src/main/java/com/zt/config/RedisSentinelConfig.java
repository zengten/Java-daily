package com.zt.config;

import io.lettuce.core.ReadFrom;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ZT
 * @version 1.0
 * @date 2022/11/28 22:49
 */
@Configuration
public class RedisSentinelConfig {

    @Bean
    @ConditionalOnProperty(prefix = "spring.redis", name = "enable", havingValue = "sentinel")
    public LettuceClientConfigurationBuilderCustomizer clientConfigurationBuilderCustomizer(){
        return clientConfigurationBuilder -> clientConfigurationBuilder.readFrom(ReadFrom.REPLICA_PREFERRED);
    }

}
