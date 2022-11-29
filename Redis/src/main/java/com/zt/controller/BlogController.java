package com.zt.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.zt.annotation.NoLogin;
import com.zt.dto.Result;
import com.zt.dto.UserDTO;
import com.zt.entity.Blog;
import com.zt.service.IBlogService;
import com.zt.service.IUserService;
import com.zt.utils.CaffeineCacheConstant;
import com.zt.utils.SystemConstants;
import com.zt.utils.UserHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author ZT
 */
@RestController
@RequestMapping("/blog")
public class BlogController {

    @Resource
    private IBlogService blogService;
    @Resource
    private IUserService userService;

    @Resource
    private Cache<String, Result> cache;

    @PostMapping
    public Result saveBlog(@RequestBody Blog blog) {
        return blogService.saveBlog(blog);
    }


    /**
     * 获取用户的评论
     */
    @GetMapping("/{id}")
    @NoLogin
    public Result of(@PathVariable("id") Long id) {
        return blogService.getBlogById(id);
    }

    /**
     * 用户点赞和取消
     */
    @PutMapping("/like/{id}")
    public Result likeBlog(@PathVariable("id") Long id) {
        return blogService.likeBlog(id);
    }

    /**
     * 点赞排行榜
     * 使用set数据结构实现点赞，但此时点赞列表是无序的
     * 需要按点赞时间排序，则使用 zSet 数据结构
     */
    @GetMapping("/likes/{id}")
    @NoLogin
    public Result likesList(@PathVariable("id") Long id) {
        return blogService.likesList(id);
    }

    @GetMapping("/of/me")
    public Result queryMyBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        // 获取登录用户
        UserDTO user = UserHolder.getUser();
        // 根据用户查询
        Page<Blog> page = blogService.query()
                .eq("user_id", user.getId()).page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        return Result.ok(records);
    }

    /**
     * 获取热门点评
     * 使用caffeine Cache查询，实际使用应在service中
     */
    @GetMapping("/hot")
    @NoLogin
    public Result queryHotBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        return cache.get(CaffeineCacheConstant.BLOG_HOT + current, key -> blogService.queryHotBlog(current));
    }


    /**
     * 查询用户的blog
     */
    @GetMapping("/of/user")
    public Result queryBlogByUserId(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam("id") Long id) {
        // 根据用户查询
        Page<Blog> page = blogService.query()
                .eq("user_id", id).page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        return Result.ok(records);
    }

    /**
     * 获取关注用户的动态
     * 基于推模式实现 feed流
     * 注意与拉模式，推拉结合模式的比较
     */
    @GetMapping("/of/follow")
    public Result getFollowBlog(@RequestParam Long lastId,
                                @RequestParam(defaultValue = "0") Integer offset) {
        return blogService.getFollowBlog(lastId, offset);
    }
}
