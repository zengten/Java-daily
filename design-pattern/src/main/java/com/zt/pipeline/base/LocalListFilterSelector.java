package com.zt.pipeline.base;

import java.util.ArrayList;
import java.util.List;

/**
 * FilterSelector 默认实现
 */
public class LocalListFilterSelector implements FilterSelector {

    private List<String> filterList = new ArrayList<>();

    @Override
    public boolean matchFilter(String currentFilterName) {
        return filterList.stream().anyMatch(item -> item.equals(currentFilterName));
    }

    @Override
    public List<String> getFilterNames() {
        return filterList;
    }


    public List<String> addFilter(String filterName) {
        filterList.add(filterName);
        return filterList;
    }

}
