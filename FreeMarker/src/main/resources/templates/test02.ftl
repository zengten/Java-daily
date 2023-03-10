<html>
<head>
    <title>展示数据类型</title>
</head>
<body>
<h6>布尔类型，${flag?c} &nbsp; ${flag?string} &nbsp; ${flag?string("yes", "no")}</h6>
<h6>日期类型，${time?date} &nbsp; ${time?time} &nbsp; ${time?datetime} &nbsp; ${time?string("yyyy年MM⽉dd⽇ HH:mm:ss")}</h6>
<#--doubleValue 会有精度问题-->
<h6>数值类型，${intValue?c} &nbsp; ${doubleValue?c} &nbsp; ${doubleValue?string}</h6>
<#--string.currency 货币格式 percent 百分比格式-->
<h6>数值类型，${doubleValue?string.currency} &nbsp; ${avgValue?string.percent} &nbsp; ${avgValue?string["0.##"]}</h6>
<#--数据类型：字符串类型
        在freemarker中字符串类型可以直接输出；
            1. 截取字符串（左闭右开） ?substring(start,end)
            2. ⾸字⺟⼩写输出 ?uncap_first
            3. ⾸字⺟⼤写输出 ?cap_first
            4. 字⺟转⼩写输出   ?lower_case
            5. 字⺟转⼤写输出   ?upper_case
            6. 获取字符串⻓度 ?length
            7. 是否以指定字符开头（boolean类型） ?starts_with("xx")?string
            8. 是否以指定字符结尾（boolean类型） ?ends_with("xx")?string
            9. 获取指定字符的索引 ?index_of("xx")
            10. 去除字符串前后空格 ?trim
            11. 替换指定字符串 ?replace("xx","xx")-->
<h6>字符串类型，${str} &nbsp; ${str?lower_case} &nbsp; ${str?upper_case}</h6>
</body>
</html>