package com.lsh.strategy.dao;

import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * Created by zhengxuefeng on 2016/11/7.
 */
@Repository
public class RedisStringDao {

    @Resource(name = "redisTemplate_r")
    private ValueOperations<String, String> valOp_r;

//    @Resource(name = "redisTemplate_w")
//    private ValueOperations<String, String> valOp_w;


//    public Long increment(String key) {
//        return valOp_w.increment(key, 1L);
//    }

    public String get(String key) {
        return valOp_r.get(key);
    }

//    public void set(String key, Object value) {
//        if (StringUtils.isBlank(key)) {
//            return;
//        }
//        String valueStr = ObjUtils.toString(value, "");
//        if (StringUtils.isBlank(valueStr)) {
//            valueStr = NULL_STR;
//        }
//        valOp_w.set(key, valueStr);
//    }

//    public void increase(String key, double val) {
//        valOp_w.increment(key, val);
//    }
//
//    public void decrease(String key, double val) {
//        valOp_w.increment(key, 0-val);
//    }
}
