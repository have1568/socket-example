package net.wang.socket.udp;

public class MessageBuilder {

    public static final String SB_PREFIX = "收到暗号，我是(SN)";
    public static final String PORT_PREFIX = "这是暗号，请回电端口 (PORT) :";

    public static String buildWithPort(int port) {
        return PORT_PREFIX + port;
    }

    public static int parsePort(String data) {
        if (data.startsWith(PORT_PREFIX)) {
            return Integer.parseInt(data.substring(PORT_PREFIX.length()));
        }

        return -1;
    }

    public static String buildWithSN(String sn) {
        return SB_PREFIX + sn;
    }

    public static int parseSN(String data) {
        if (data.startsWith(SB_PREFIX)) {
            return Integer.parseInt(data.substring(SB_PREFIX.length()));
        }

        return -1;
    }

}
