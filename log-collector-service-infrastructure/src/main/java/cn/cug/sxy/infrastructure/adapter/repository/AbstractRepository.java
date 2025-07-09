package cn.cug.sxy.infrastructure.adapter.repository;

import cn.cug.sxy.infrastructure.redis.IRedisService;
import jakarta.annotation.Resource;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @version 1.0
 * @Date 2025/7/2 14:52
 * @Description 抽象仓储
 * @Author jerryhotton
 */

public class AbstractRepository {

    @Resource
    private IRedisService redisService;

    protected <T> T getDataFromCacheOrDB(String cacheKey, long expireTime, Supplier<T> call) {
        T cacheResult = redisService.getValue(cacheKey);
        // 缓存中存在则直接返回
        if (null != cacheResult) {
            return cacheResult;
        }
        // 缓存中不存在则查询数据库
        T dbResult = call.get();
        // 写入缓存
        if (expireTime > 0) {
            redisService.setValue(cacheKey, dbResult, expireTime, TimeUnit.MILLISECONDS);
        } else {
            redisService.setValue(cacheKey, dbResult);
        }
        return dbResult;
    }

    protected <T> T getDataFromCacheOrDB(String cacheKey, Supplier<T> call) {
        return getDataFromCacheOrDB(cacheKey, -1, call);
    }

}
