package com.batchtask.manage.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * Redis工具类
 * @author yh
 */
@Component
public class RedisUtil {

    private static final Logger logger = LoggerFactory.getLogger(RedisUtil.class);

    private final StringRedisTemplate redisTemplate;

    @Autowired
    public RedisUtil(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 加锁操作
     * @param key 锁键 使用batchTask的taskKey
     * @param value 当前时间+超时时间
     * @return 加锁是否成功
     */
    public boolean lock(String key,String value) {
        if (redisTemplate.opsForValue().setIfAbsent(key, value)) {
            return true;
        }

        String cValue = redisTemplate.opsForValue().get(key);
        //判断锁过期避免死锁
        if (StringUtils.isNotEmpty(cValue) && Long.parseLong(cValue) < System.currentTimeMillis()) {
            //拿到上一个锁
            String befValue = redisTemplate.opsForValue().getAndSet(key, value);

            //上一个锁与当前锁一致则将锁给与当前线程，新进线程无法获取
            return cValue.equals(befValue);
        }
        return false;
    }

    /**
     * 释放锁操作
     * @param key 锁键 使用batchTask的taskKey
     * @param value 当前时间+超时时间
     */
    public void unlock(String key,String value) {
        try {
            String cValue = redisTemplate.opsForValue().get(key);
            //判断锁是否还存在
            if (StringUtils.isNotEmpty(cValue) && cValue.equals(value)) {
                //释放锁
                redisTemplate.opsForValue().getOperations().delete(key);
            }
        } catch (Exception e) {
            logger.error("redis释放锁异常,{}",CommonUtil.getStackTrace(e));
        }
    }

    /**
     * 根据map写入缓存
     * @param map 需要写入缓存的配置
     */
    public void set(final Map<String,String> map) {
        for (Map.Entry<String,String> entry : map.entrySet()) {
            set(entry.getKey(),entry.getValue());
        }
    }

    /**
     * 写入缓存
     * @param key 需要写入的key
     * @param value 需要写入的value
     * @return 是否写入成功
     */
    public boolean set(final String key, String value) {
        boolean result = false;
        try {
            ValueOperations<String, String> operations = redisTemplate.opsForValue();
            operations.set(key, value);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 写入缓存
     * @param key 需要写入的key
     * @param value 需要写入的value
     * @param expireTime 过期时间
     * @return 是否写入成功
     */
    public boolean set(final String key, String value, Long expireTime) {
        boolean result = false;
        try {
            ValueOperations<String, String> operations = redisTemplate.opsForValue();
            operations.set(key, value);
            redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 删除对应的value
     * @param key 需要删除的key
     */
    public void remove(final String key) {
        if (exists(key)) {
            redisTemplate.delete(key);
        }
    }

    /**
     * 判断缓存中是否有对应的value
     * @param key 需要判断的key
     * @return 是否存在对应的值
     */
    public boolean exists(final String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 读取缓存
     * @param key 读取的key
     * @return 对应的value
     */
    public String get(final String key) {
        Object result;
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        result = operations.get(key);
        if(result==null){
            return null;
        }
        return result.toString();
    }

}
