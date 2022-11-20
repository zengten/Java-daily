package com.zt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zt.dto.Result;
import com.zt.entity.Follow;

/**
 * @author ZT
 */
public interface IFollowService extends IService<Follow> {

    Result isFollow(Long id);

    Result follow(Long id, Boolean isFollow);

    Result commonFollow(Long id);

}
