package com.zt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zt.dto.Result;
import com.zt.entity.Shop;

/**
 * @author ZT
 */
public interface IShopService extends IService<Shop> {

    Result queryShopById(Long id);

    Result updateShopById(Shop shop);

    Result queryShopByType(Integer typeId, Integer current, Double x, Double y);

}
