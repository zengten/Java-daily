package com.zt.easyExcel.model;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.alibaba.excel.converters.string.StringImageConverter;
import lombok.Builder;
import lombok.Data;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

/**
 * 当没有使用@ExcelProperty时，自动使用字段名作为excel sheet的头
 */
@Data
@Builder
@ContentRowHeight(100)  // 内容高度
@ColumnWidth(100)   // 列宽
@HeadRowHeight(40)  //头高度
public class ImageData {
    /**
     * 使用抽象文件表示一个图片
     */
    private File file;
    /**
     * 使用输入流保存一个图片
     */
    private InputStream inputStream;
    /**
     * 使用imagePath保存一个图片的路径时  需要使用StringImageConverter转换器
     */
    @ExcelProperty(converter = StringImageConverter.class)
    private String imagePath;
    /**
     * 使用二进制数据保存为一个图片
     */
    private byte[] byteArray;
    /**
     * 使用网络链接保存一个图片
     */
    private URL url;
}