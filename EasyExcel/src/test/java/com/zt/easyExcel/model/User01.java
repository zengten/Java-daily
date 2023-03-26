package com.zt.easyExcel.model;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 只含有 @ExcelProperty 注解的实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User01 {

    @ExcelProperty("用户编号")
    private Integer userId;
    @ExcelProperty("姓名")
    private String userName;
    @ExcelProperty("性别")
    private String gender;
    @ExcelProperty("工资")
    private Double salary;
    @ExcelProperty("入职时间")
    private Date hireDate;

}