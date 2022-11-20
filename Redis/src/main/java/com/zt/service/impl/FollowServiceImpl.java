package com.zt.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zt.dto.Result;
import com.zt.dto.UserDTO;
import com.zt.entity.Follow;
import com.zt.entity.User;
import com.zt.mapper.FollowMapper;
import com.zt.service.IFollowService;
import com.zt.service.IUserService;
import com.zt.utils.RedisConstants;
import com.zt.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author ZT
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    @Autowired
    private IUserService userService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result isFollow(Long id) {
        User user = userService.getById(id);
        if(ObjectUtil.isNull(user)) {
            return Result.fail("用户不存在！");
        }
        return Result.ok(isFollow(UserHolder.getUser().getId(), id));
    }


    /**
     * 判断是否关注该用户
     * @param curUserId 当前登录用户
     * @param id 目标用户id
     * @return true or false
     */
    private boolean isFollow(Long curUserId, Long id) {
        Follow follow = getOne(Wrappers.<Follow>lambdaQuery().eq(Follow::getUserId, curUserId)
                .eq(Follow::getFollowUserId, id)
                .last(" limit 1"));
        return ObjectUtil.isNotNull(follow);
    }

    @Override
    public Result follow(Long id, Boolean isFollow) {
        Long curUserId = UserHolder.getUser().getId();
        if(isFollow) {
            // 重复关注
            if(isFollow(curUserId, id)) {
                return Result.fail("已经关注过了！");
            }
            save(Follow.builder()
                    .userId(curUserId)
                    .followUserId(id)
                    .build());
            stringRedisTemplate.opsForSet().add(RedisConstants.FOLLOW_KEY + curUserId, String.valueOf(id));
        } else {
            remove(Wrappers.<Follow>lambdaQuery()
                    .eq(Follow::getUserId, curUserId)
                    .eq(Follow::getFollowUserId, id));
            stringRedisTemplate.opsForSet().remove(RedisConstants.FOLLOW_KEY + curUserId, String.valueOf(id));
        }
        return Result.ok();
    }

    @Override
    public Result commonFollow(Long id) {
        String curKey = RedisConstants.FOLLOW_KEY + UserHolder.getUser().getId();
        String targetKey = RedisConstants.FOLLOW_KEY + id;
        Set<String> commonFollowUserIdSet = stringRedisTemplate.opsForSet().intersect(curKey, targetKey);
        if(CollectionUtil.isEmpty(commonFollowUserIdSet)) {
            return Result.ok(ListUtil.empty());
        }
        List<UserDTO> userDTOList = userService.list(Wrappers.<User>lambdaQuery().in(User::getId, commonFollowUserIdSet))
                .stream()
                .map(user -> BeanUtil.copyProperties(user, UserDTO.class))
                .collect(Collectors.toList());
        return Result.ok(userDTOList);
    }
}
