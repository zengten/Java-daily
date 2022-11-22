package com.zt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zt.entity.BlogComments;
import com.zt.mapper.BlogCommentsMapper;
import com.zt.service.IBlogCommentsService;
import org.springframework.stereotype.Service;

/**
 * @author ZT
 */
@Service
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments> implements IBlogCommentsService {

}
