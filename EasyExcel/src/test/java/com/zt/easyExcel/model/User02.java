package com.zt.easyExcel.model;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 在 @ExcelProperty(index = 1) 的实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User02 {

    @ExcelProperty(value = "用户编号", index = 0)
    private Integer userId;
    @ExcelProperty(value = "姓名", index = 1)
    private String userName;
    @ExcelProperty(value = "性别", index = 2)
    private String gender;
    @ExcelProperty(value = "工资", index = 3)
    private Double salary;
    @ExcelProperty(value = "入职时间", index = 4)
    private Date hireDate;

}