package com.zt.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zt.dto.Result;
import com.zt.dto.ScrollResult;
import com.zt.dto.UserDTO;
import com.zt.entity.Blog;
import com.zt.entity.Follow;
import com.zt.entity.User;
import com.zt.mapper.BlogMapper;
import com.zt.service.IBlogService;
import com.zt.service.IFollowService;
import com.zt.service.IUserService;
import com.zt.utils.RedisConstants;
import com.zt.utils.SystemConstants;
import com.zt.utils.UserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author ZT
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

    @Autowired
    private IUserService userService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private IFollowService followService;

    @Override
    public Result getBlogById(Long id) {
        Blog blog = getById(id);
        if(Objects.isNull(blog)) {
            return Result.fail("不存在！");
        }
        queryUserMsg(blog);
        blog.setIsLike(blogIsLiked(blog.getId()));
        return Result.ok(blog);
    }

    /**
     * 是否被当前用户点赞
     */
    private boolean blogIsLiked(Long blogId) {
        String redisKey = RedisConstants.BLOG_LIKED_KEY + blogId;
        UserDTO user = UserHolder.getUser();
        if(ObjectUtil.isNull(user)) {
            return false;
        }
        String userId = String.valueOf(user.getId());
        Double isLiked = stringRedisTemplate.opsForZSet().score(redisKey, userId);
        return ObjectUtil.isNotNull(isLiked);
    }

    @Override
    public Result queryHotBlog(Integer current) {
        // 根据用户查询
        Page<Blog> page = query()
                .orderByDesc("liked")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        // 查询用户
        records.forEach(blog -> {
            queryUserMsg(blog);
            blog.setIsLike(blogIsLiked(blog.getId()));
        });
        return Result.ok(records);
    }

    /**
     * 使用redis zSet结构 保存点赞情况
     */
    @Override
    public Result likeBlog(Long id) {
        String userId = String.valueOf(UserHolder.getUser().getId());
        if(blogIsLiked(id)) {
            // 已点赞 -> 取消点赞
            lambdaUpdate().setSql("liked = liked - 1").eq(Blog::getId, id).update();
            stringRedisTemplate.opsForZSet().remove(RedisConstants.BLOG_LIKED_KEY + id, userId);
        } else {
            // 进行点赞
            lambdaUpdate().setSql("liked = liked + 1").eq(Blog::getId, id).update();
            // 当前时间作为 zSet的排序值
            stringRedisTemplate.opsForZSet().add(RedisConstants.BLOG_LIKED_KEY + id, userId, DateUtil.current());
        }
        return Result.ok();
    }

    /**
     * set结构随机排序取前2个：opsForSet().randomMembers(key, 2)
     * zSet结构实现点赞排行榜，按点赞时间排序，并查询点赞的前5数据
     */
    @Override
    public Result likesList(Long id) {
        Set<String> top5 = stringRedisTemplate.opsForZSet().range(RedisConstants.BLOG_LIKED_KEY + id, 0, 4);
        if(CollectionUtil.isEmpty(top5)) {
            return Result.ok(ListUtil.empty());
        }
        // in 默认按id升序排序，不符合点赞排行榜要求，使用 order by field(id,5,1,1010) 解决
        String lastSql = StrUtil.format(" order by field(id,{})", String.join(",", top5));
        List<User> userList = userService.list(Wrappers.<User>lambdaQuery()
                .in(User::getId, top5)
                .last(lastSql));
        List<UserDTO> userDTOList = BeanUtil.copyToList(userList, UserDTO.class);
        return Result.ok(userDTOList);
    }

    @Override
    public Result saveBlog(Blog blog) {
        // 获取登录用户
        UserDTO user = UserHolder.getUser();
        blog.setUserId(user.getId());
        // 保存探店博文
        boolean isSuccess = save(blog);
        if (!isSuccess) {
            return Result.fail("保存失败！");
        }
        // 实现feed推送流
        pushBlogToFollowUser(blog.getId());
        // 返回id
        return Result.ok(blog.getId());
    }

    /**
     * reverseRangeByScore 不查询score，reverseRangeWithScore 查询score
     */
    @Override
    public Result getFollowBlog(Long lastId, Integer offset) {
        Long curUserId = UserHolder.getUser().getId();
        String key = RedisConstants.FEED_KEY + curUserId;
        Set<ZSetOperations.TypedTuple<String>> typedTupleSet = stringRedisTemplate.opsForZSet()
                // 对score范围查询，范围[0,lastId]，倒序跳过offset条，共查询count条
                .reverseRangeByScoreWithScores(key, 0, lastId, offset, 2);
        if (CollectionUtil.isEmpty(typedTupleSet)) {
            return Result.ok(ListUtil.empty());
        }
        List<String> blogIdList = ListUtil.list(false);
        // 至少1才能跳过lastId
        int lastOffSetCount = 1;
        long minTime = 0L;
        for (ZSetOperations.TypedTuple<String> tuple : typedTupleSet) {
            blogIdList.add(tuple.getValue());
            Long score = Convert.toLong(tuple.getScore());
            // 时间相等则计数自增，不等则跳过并赋初值
            if(minTime == score) {
                lastOffSetCount++;
            } else {
                minTime = score;
                lastOffSetCount = 1;
            }
        }
        // 查询动态
        String lastSql = StrUtil.format(" order by field(id,{})", String.join(",", blogIdList));
        List<Blog> blogList = list(Wrappers.<Blog>lambdaQuery().in(Blog::getId, blogIdList).last(lastSql));
        for (Blog blog : blogList) {
            blog.setIsLike(blogIsLiked(blog.getId()));
            queryUserMsg(blog);
        }
        // 包装返回
        ScrollResult scrollResult = new ScrollResult();
        scrollResult.setList(blogList);
        scrollResult.setMinTime(minTime);
        scrollResult.setOffset(lastOffSetCount);
        return Result.ok(scrollResult);
    }

    /**
     * 推送 blog 信息到粉丝
     */
    private void pushBlogToFollowUser(Long id) {
        // 查询所有粉丝用户
        List<Follow> followList = followService.list(Wrappers.<Follow>lambdaQuery()
                .eq(Follow::getFollowUserId, UserHolder.getUser().getId()));
        if(CollectionUtil.isEmpty(followList)) {
            return;
        }
        long curTime = DateUtil.current();
        // 推送到所有粉丝
        for (Follow follow : followList) {
            String key = RedisConstants.FEED_KEY + follow.getUserId();
            stringRedisTemplate.opsForZSet().add(key, String.valueOf(id), curTime);
        }
    }

    /**
     * 查询用户信息
     */
    private void queryUserMsg(Blog blog) {
        Long userId = blog.getUserId();
        User user = userService.getById(userId);
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
    }
}
