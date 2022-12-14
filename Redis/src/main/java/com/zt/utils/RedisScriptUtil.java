package com.zt.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * redis lua script util
 */
@Component
@Slf4j
public class RedisScriptUtil {

    public enum ScriptEnum {

        /**
         * 秒杀脚本
         */
        @SuppressWarnings("SpellCheckingInspection")
        SECKILL("seckill", "/lua/seckill.lua"),
        /**
         * 签到脚本
         */
        SIGN("sign", "/lua/sign.lua");

        /**
         * 脚本名称
         */
        private String name;

        /**
         * 脚本值(存储路径)
         */
        private String value;

        ScriptEnum(String name, String value) {
            this.name = name;
            this.value = value;
        }

    }

    private static final Map<String, RedisScript<Long>> SCRIPT_MAP = new HashMap<>();


    public RedisScript<Long> getRedisScript(ScriptEnum script) {
        return SCRIPT_MAP.get(script.name);
    }

    @PostConstruct
    private void init() {
        for (ScriptEnum scriptEnum : ScriptEnum.values()) {
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setResultType(Long.class);
            redisScript.setLocation(new ClassPathResource(scriptEnum.value));
            SCRIPT_MAP.put(scriptEnum.name, redisScript);
        }
        log.debug("redis script init success...");
    }


}
