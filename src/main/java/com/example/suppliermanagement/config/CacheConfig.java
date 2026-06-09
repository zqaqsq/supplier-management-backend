package com.example.suppliermanagement.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 缓存配置
 * 使用 Caffeine 作为本地缓存
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 供应商列表缓存名称
     */
    public static final String SUPPLIER_LIST_CACHE = "supplierList";

    /**
     * 资质等级列表缓存名称
     */
    public static final String QUALIFICATIONS_CACHE = "qualifications";

    /**
     * 地区列表缓存名称
     */
    public static final String REGIONS_CACHE = "regions";

    /**
     * 经营状态列表缓存名称
     */
    public static final String STATUSES_CACHE = "statuses";

    /**
     * 配置缓存管理器
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                SUPPLIER_LIST_CACHE,
                QUALIFICATIONS_CACHE,
                REGIONS_CACHE,
                STATUSES_CACHE
        );
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }

    /**
     * 配置 Caffeine 缓存规格
     */
    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                // 初始容量
                .initialCapacity(100)
                // 最大缓存数量
                .maximumSize(1000)
                // 访问后过期时间（写操作后不重置）
                .expireAfterAccess(30, TimeUnit.MINUTES)
                // 写操作后异步记录统计
                .recordStats()
                // 缓存不存在时的异步加载（可选）
                .removalListener((key, value, cause) -> {
                    // 缓存被移除时的日志记录
                    System.out.println("缓存被移除: key=" + key + ", 原因=" + cause);
                });
    }
}
