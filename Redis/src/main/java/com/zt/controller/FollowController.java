package com.zt.controller;


import com.zt.dto.Result;
import com.zt.service.IFollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author ZT
 */
@RestController
@RequestMapping("/follow")
public class FollowController {

    @Autowired
    private IFollowService followService;


    /**
     * 关注 or 取关
     * @param id 操作用户id
     * @param isFollow true进行关注 false去关
     */
    @PutMapping("/{id}/{isFollow}")
    public Result follow(@PathVariable Long id,
                         @PathVariable Boolean isFollow) {
        return followService.follow(id, isFollow);
    }

    /**
     * 是否关注该用户
     */
    @GetMapping("/or/not/{id}")
    public Result isFollow(@PathVariable Long id) {
        return followService.isFollow(id);
    }


    @GetMapping("/common/{id}")
    public Result commonFollow(@PathVariable Long id) {
        return followService.commonFollow(id);
    }
}
