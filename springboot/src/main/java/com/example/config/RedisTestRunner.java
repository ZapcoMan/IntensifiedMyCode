package com.example.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

import com.example.utils.RedisUtils;

import java.util.concurrent.TimeUnit;

/**
 * Redis连接测试组件
 * 在应用启动时测试Redis连接是否正常
 */
@Component
public class RedisTestRunner implements CommandLineRunner {

    @Resource
    private RedisUtils redisUtils;

    private static final Log log = LogFactory.getLog(RedisTestRunner.class);

    @Override
    public void run(String... args) throws Exception {
        try {
            // 测试Redis连接
            String testKey = "test:connection:" + System.currentTimeMillis();
            String testValue = "Redis连接测试成功";
            
            // 设置测试值 并设置60秒过期时间
            redisUtils.set(testKey, testValue, 60, TimeUnit.SECONDS);
            // 获取测试值
            Object result = redisUtils.get(testKey);
            
            if (testValue.equals(result)) {
                log.info("✅ Redis连接测试成功！");
            } else {
                log.warn("❌ Redis连接测试失败！ 服务可能未启动");
            }
            
            // 清理测试数据
            redisUtils.remove(testKey);
            
        } catch (Exception e) {
            log.warn("❌ Redis连接测试失败：" + e.getMessage() + "\t 服务可能未启动!");
            e.printStackTrace();
        }
    }
}