package net.wang.socket.udp.search.bean;

import lombok.Data;

/**
 * 服务端信息类
 */
@Data
public class ServerInfo {
    private String sn;
    private int port;
    private String address;

    public ServerInfo(int port, String ip, String sn) {
        this.port = port;
        this.address = ip;
        this.sn = sn;
    }


}
