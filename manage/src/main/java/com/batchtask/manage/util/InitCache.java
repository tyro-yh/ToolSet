package com.batchtask.manage.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 初始化加载配置
 * @author yh
 */
@Component
public class InitCache implements ApplicationRunner {

    private final RedisUtil redisUtil;

    @Autowired
    public  InitCache(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    @Override
    public void run(ApplicationArguments args) {
        //todo 获取需要加载的配置 可以从数据库读取或者走配置文件
        Map<String,String> map = new HashMap<>(1);
        map.put("test","1");
        redisUtil.set(map);
        String value = redisUtil.get("test");
        System.out.print("缓存："+value);
    }
}
