package com.zt.config;

import io.lettuce.core.ReadFrom;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStaticMasterReplicaConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.util.List;

/**
 * 配置redis主从
 */
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Getter
@Setter
public class RedisMasterAndSlaveConfig {

    private RedisInstance master;
    private List<RedisInstance> slaves;

    @Bean
    @ConditionalOnProperty(prefix = "spring.redis", name = "enable", havingValue = "cluster")
    public LettuceConnectionFactory redisConnectionFactory() {
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .readFrom(ReadFrom.REPLICA_PREFERRED)
                .build();
        RedisStaticMasterReplicaConfiguration staticMasterReplicaConfiguration =
                new RedisStaticMasterReplicaConfiguration(this.getMaster().getHost(),
                        this.getMaster().getPort());
        this.getSlaves().forEach(slave ->
                staticMasterReplicaConfiguration.addNode(slave.getHost(), slave.getPort()));
        return new LettuceConnectionFactory(staticMasterReplicaConfiguration, clientConfig);
    }

    @Getter
    @Setter
    private static class RedisInstance {

        private String host;
        private int port;

    }

}