package com.zt.strategy;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * type
 */
@AllArgsConstructor
@Getter
public enum StrategyTypeEnum {

    SMS(1),

    APP(2);

    private int code;


}
