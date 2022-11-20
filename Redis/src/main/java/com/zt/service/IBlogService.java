package com.zt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zt.dto.Result;
import com.zt.entity.Blog;

/**
 * @author ZT
 */
public interface IBlogService extends IService<Blog> {

    Result getBlogById(Long id);

    Result queryHotBlog(Integer current);

    Result likeBlog(Long id);

    Result likesList(Long id);

    Result saveBlog(Blog blog);

    Result getFollowBlog(Long lastId, Integer offset);

}
