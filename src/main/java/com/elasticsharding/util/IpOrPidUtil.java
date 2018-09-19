package com.elasticsharding.util;

import org.apache.commons.lang3.StringUtils;

import java.lang.management.ManagementFactory;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @desc : ip和进程id工具类
 * @author: guanjie
 * @date : 2018/09/05
 */
public class IpOrPidUtil {


    /**
     * 获取本机ip
     *
     * @return
     */
    public static String getLocalIp() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> iNetAddresses = networkInterface.getInetAddresses();
                while (iNetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = iNetAddresses.nextElement();
                    if (inetAddress instanceof Inet4Address && !StringUtils.equals("127.0.0.1", inetAddress.getHostAddress())) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {

        }
        return null;
    }

    /**
     * 获取当前进程id
     *
     * @return
     */
    public static String getPid() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        return name.split("@")[0];
    }
}
