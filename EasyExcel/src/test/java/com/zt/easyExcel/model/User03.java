package com.zt.easyExcel.model;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.format.NumberFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 时间和数值格式化 实体类   同时指定表头高度、列宽、内容单元格高度
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ContentRowHeight(20)  // 内容高度
@ColumnWidth(30)   // 列宽
@HeadRowHeight(40)  //头高度
public class User03 {

    @ExcelProperty("用户编号")
    private Integer userId;
    @ExcelProperty("姓名")
    private String userName;
    @ExcelProperty("性别")
    private String gender;
    @ExcelProperty("工资")
    @NumberFormat("#.##")
    private Double salary;
    @ExcelProperty("入职时间")
    @DateTimeFormat("yyyy年MM月dd日 HH时mm分ss秒")
    private Date hireDate;

}