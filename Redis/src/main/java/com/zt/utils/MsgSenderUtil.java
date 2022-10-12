package com.zt.utils;

import com.zt.dto.MsgVo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MsgSenderUtil {

    public static void sendMessage(MsgVo msgVo) {
        // TODO
        log.info(">>>>>sms msg = {}", msgVo.getMsg());
    }

}
