package com.smartcloud.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ConnectionHelper {
    public static InetAddress inetAddressFromInt(int addr) {
        try {
            return Inet4Address.getByAddress(new byte[]{
                    (byte) addr, (byte) (addr >>> 8), (byte) (addr >>> 16), (byte) (addr >>> 24)});
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String byteArrayToIpAddress(byte[] bytes) {
        String address = Integer.toString((bytes[0] + 256) % 256);
        for (int i = 1; i < bytes.length; i++) {
            address += "." + (bytes[i] + 256) % 256;
        }
        return address;
    }
}
