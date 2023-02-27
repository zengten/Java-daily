package com.zt.bio.chat;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.net.Socket;

/**
 * @author ZT
 * @version 1.0
 * @description:
 * @date 2023/1/18 20:56
 */
@Data
@AllArgsConstructor
public class ISocket {

    private String id;

    private Socket socket;

}
