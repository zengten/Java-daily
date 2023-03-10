package com.zt.pipeline.base;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务编码枚举
 */
@AllArgsConstructor
@Getter
public enum BizEnum {

    BIZ_XXX(1, "业务编码1");

    private Integer code;

    private String name;

}
