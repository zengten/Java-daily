package com.zt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zt.dto.Result;
import com.zt.entity.Shop;
import com.zt.mapper.ShopMapper;
import com.zt.service.IShopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
@Slf4j
@CacheConfig(cacheNames = "cache:shop")
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Cacheable(key = "#id")
    @Override
    public Result queryShopById(Long id) {
        return Result.ok(getById(id));
    }

    @CacheEvict(key = "#shop.id")
    @Override
    public Result updateShopById(Shop shop) {
        boolean b = updateById(shop);
        return Result.ok();
    }
}
