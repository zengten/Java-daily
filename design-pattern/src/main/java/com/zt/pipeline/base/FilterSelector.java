package com.zt.pipeline.base;


import java.util.List;

/**
 * biz 选择器，控制跳链情况
 */
public interface FilterSelector {


    /**
     * 是否匹配当前Filter
     */
    boolean matchFilter(String currentFilterName);


    List<String> getFilterNames();

}
