package com.lsh.strategy.dao;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhengxuefeng on 2016/11/7.
 */

public class RedisBaseDao {
    protected static final String NULL_STR = "__null__";

    @Resource(name = "redisTemplate_w")
    protected RedisTemplate redisTemplate;

    /**
     * 删除缓存
     *
     * @param key
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }
    /**
     * 设置key 多久失效
     * @param key
     * @param timeout 秒
     */
    public void expire(String key,long timeout){
        redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
    }
}
