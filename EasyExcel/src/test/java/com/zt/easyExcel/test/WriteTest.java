package com.zt.easyExcel.test;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.zt.easyExcel.model.ImageData;
import com.zt.easyExcel.model.User01;
import com.zt.easyExcel.model.User02;
import com.zt.easyExcel.model.User03;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author ZT
 * @version 1.0
 * @date 2023/3/26 12:18
 */
public class WriteTest {


    /**
     * 单个文件导出
     */
    @Test
    public void testWriteSingleSheet() {
        //路径文件名
        String fileName = "user001.xlsx";
        //写入excel中的数据
        //@ExcelProperty是easyExcel提供的注解,用来定义表格的头部
        User01 user = User01.builder()
                .userName("张三")
                .gender("男")
                .hireDate(new Date())
                .salary(2.0123)
                .userId(100)
                .build();
        ArrayList<User01> users = new ArrayList<User01>() {{
            add(user);
            add(user);
            add(user);
        }};
        //写入excel，定义sheet名
        EasyExcel.write(fileName, User01.class)
                .sheet("用户信息")
                .doWrite(users);
        //文件流会自动关闭
    }

    /**
     * 多个sheet导出
     */
    @Test
    public void testWriteMultiSheet() {
        //路径文件名
        String fileName = "user002.xlsx";
        User01 user = User01.builder()
                .userName("张三")
                .gender("男")
                .hireDate(new Date())
                .salary(2.0123)
                .userId(100)
                .build();
        ArrayList<User01> users = new ArrayList<User01>() {{
            add(user);
            add(user);
            add(user);
        }};
        //构建excelWriter对象，需要文件名/输出流 和 excel头信息
        ExcelWriter excelWriter = EasyExcel.write(fileName, User01.class).build();
        //会连续写到5个不同的sheet中
        for (int i = 0; i < 5; i++) {
            WriteSheet writeSheet = EasyExcel.writerSheet(i, "用户信息" + i).build();
            excelWriter.write(users, writeSheet);
        }
        //关闭excelWriter
        excelWriter.finish();
    }

    /**
     * 只包含某些列的导出，排除某些列的导出
     */
    @Test
    public void testWriteIncludeFieldAndExcludeField() {
        String fileName1 = "user003.xlsx";
        String fileName2 = "user004.xlsx";
        User01 user = User01.builder()
                .userName("张三")
                .gender("男")
                .hireDate(new Date())
                .salary(2.0123)
                .userId(100)
                .build();
        ArrayList<User01> users = new ArrayList<User01>() {{
            add(user);
            add(user);
            add(user);
        }};
        //设置排除字段头，这些字段的数据不会写到excel中
        HashSet<String> excludeFields = new HashSet<String>() {{
            add("hireDate");
            add("gender");
        }};
        //写入数据
        EasyExcel.write(fileName1, User01.class)
                // 旧版本过期方法 excludeColumnFiledNames
                .excludeColumnFieldNames(excludeFields)
                .sheet("用户信息")
                .doWrite(users);
        //设置包含字段头，excel中只会写入这些字段
        HashSet<String> includeFields = new HashSet<String>() {{
            add("hireDate");
            add("gender");
        }};
        //写入数据 includeColumnFiledNames
        EasyExcel.write(fileName2, User01.class)
                // 旧版本过期方法 includeColumnFiledNames
                .includeColumnFieldNames(includeFields)
                .sheet("用户信息")
                .doWrite(users);
    }


    /**
     * 通过index排除字段   通过index写出包含字段
     */
    @Test
    public void testWriteIncludeIndexAndExcludeIndex() {
        String fileName1 = "user005.xlsx";
        String fileName2 = "user006.xlsx";
        User02 user = User02.builder()
                .userName("张三")
                .gender("男")
                .hireDate(new Date())
                .salary(2.0123)
                .userId(100)
                .build();
        ArrayList<User02> users = new ArrayList<User02>() {{
            add(user);
            add(user);
            add(user);
        }};
        //设置排除index坐标值，这些index数据不会写到excel中，注意实体类注解得设置index值
        Set<Integer> fieldsIndex = new HashSet<Integer>() {{
            add(1);
            add(2);
        }};
        //通过index排除不写的字段
        EasyExcel.write(fileName1, User02.class)
                .excludeColumnIndexes(fieldsIndex)
                .sheet("用户信息")
                .doWrite(users);
        //通过index包含的字段
        EasyExcel.write(fileName2, User02.class)
                .includeColumnIndexes(fieldsIndex)
                .sheet("用户信息")
                .doWrite(users);
    }


    /**
     * 重复写入到同个sheet
     */
    @Test
    public void testWriteSimpleSheet() {
        String fileName = "user007.xlsx";
        User02 user = User02.builder()
                .userName("张三")
                .gender("男")
                .hireDate(new Date())
                .salary(2.0123)
                .userId(100)
                .build();
        ArrayList<User02> users = new ArrayList<User02>() {{
            add(user);
            add(user);
            add(user);
        }};
        //写入数据
        ExcelWriter excelWriter = EasyExcel.write(fileName, User02.class).build();
        WriteSheet writeSheet = EasyExcel.writerSheet(0, "用户信息").build();
        //会连续写5次到sheet，一共15行数据
        for (int i = 0; i < 5; i++) {
            excelWriter.write(users, writeSheet);
        }
        excelWriter.finish();
    }

    /**
     * 数值和日期指定格式化 导出到excel中
     */
    @Test
    public void testWriteCustomizedNumberAndDate() {
        String fileName = "user008.xlsx";
        User03 user = User03.builder()
                .userName("张三")
                .gender("男")
                .hireDate(new Date())
                .salary(2.0123)
                .userId(100)
                .build();
        ArrayList<User03> users = new ArrayList<User03>() {{
            add(user);
            add(user);
            add(user);
        }};
        EasyExcel.write(fileName, User03.class)
                .sheet("用户信息")
                .doWrite(users);
    }

    /**
     * 图片填充到excel
     */
    @Test
    public void testWriteImage() throws IOException {
        String fileName = "user009.xlsx";
        String imageFilePath = "cat.jpg";
        File file = new File(imageFilePath);
        FileInputStream inputStream = new FileInputStream(imageFilePath);
        byte[] byteArray = new byte[(int) file.length()];
        inputStream.read(byteArray);
        ImageData imageData = ImageData.builder()
                .file(file)
                .byteArray(byteArray)
                //为啥必须重新new FileInputStream ?? 上面的inputStream read之后不能填充
                .inputStream(new FileInputStream(imageFilePath))
                .imagePath(imageFilePath)
                // 必须是允许调用的图片
                .url(new URL("http://s15.sinaimg.cn/mw690/001lEyUDzy7UJvhtePW54&690"))
                .build();
        ArrayList list = new ArrayList<ImageData>() {{
            add(imageData);
        }};
        //写入数据
        EasyExcel.write(fileName, ImageData.class)
                .sheet("图片填充")
                .doWrite(list);
        inputStream.close();
    }
}
