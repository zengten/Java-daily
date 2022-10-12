package com.zt.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.zt.dto.MsgVo;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class MsgSenderUtil {


    /**
     * 发送短信的请求的链接
     */
    public static final String SMS_URL = "http://api.yl1001.com/webservice/index.php?op=sms_msg&func=SendSmsMsg&gtype=http&rtype=json";

    /**
     * 获取发送短信的Token值
     */
    public static final String SMS_TOKEN = "http://api.yl1001.com/webservice/index.php?op=init_log&gtype=http&func=getAccessToken";


    public static void sendMessage(MsgVo msgVo) {
        String jsonResult = HttpUtil.post(SMS_TOKEN, getAccessToken());
        log.info("send msg token:{}", jsonResult);
        if (!StrUtil.isEmpty(jsonResult)) {
            JSONObject jsonToken = JSONUtil.parseObj(jsonResult);
            if (jsonToken.containsKey("secret") && jsonToken.containsKey("access_token")) {
                String url = SMS_URL + "&secret=" + jsonToken.getStr("secret") + "&access_token=" + jsonToken.getStr("access_token");
                HttpUtil.post(url, BeanUtil.beanToMap(msgVo));
            }
        }
    }


    /**
     * 获取发送token的json
     *
     * @return
     */
    public static Map<String, Object> getAccessToken() {
        Map<String, Object> json = new HashMap<>();
        json.put("user", "iphone");
        json.put("pwd", 123);
        json.put("time", String.valueOf(System.currentTimeMillis()));
        json.put("vflag", 1);
        return json;
    }
}
