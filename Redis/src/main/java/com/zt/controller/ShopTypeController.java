package com.zt.controller;


import com.zt.annotation.NoLogin;
import com.zt.dto.Result;
import com.zt.entity.ShopType;
import com.zt.service.IShopTypeService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author ZT
 */
@RestController
@RequestMapping("/shop-type")
public class ShopTypeController {
    @Resource
    private IShopTypeService typeService;

    @GetMapping("list")
    @NoLogin
    @Cacheable(cacheNames = "cache:shopType")
    public Result queryTypeList() {
        List<ShopType> typeList = typeService
                .query().orderByAsc("sort").list();
        return Result.ok(typeList);
    }
}
