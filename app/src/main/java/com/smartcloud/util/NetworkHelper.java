package com.smartcloud.util;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import com.smartcloud.MainActivity;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class NetworkHelper {
    public static final String SSID = "SmartCloud";

    public static WifiConfiguration getWifiConfiguration() {
        WifiConfiguration netConfig = new WifiConfiguration();
        netConfig.SSID = SSID;
//        netConfig.preSharedKey = String.format("\"%s\"", password);
//        netConfig.SSID = SSID;
//        netConfig.preSharedKey = password;
//        netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
//        netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
//        netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
//        netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
//        netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
//        netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
//        netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
//        netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        return netConfig;
    }

    public static String convertToQuotedString(String string) {
        return "\"" + string + "\"";
    }

    public static WifiManager getWifiManager() {
        return (WifiManager) MainActivity.currentContext.getSystemService(Context.WIFI_SERVICE);
    }

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

    public static InetAddress getMasterAddress() {
        List<String> arpTableRows = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
//                System.out.println("ARP TABLE: " + line);
                if (!line.equals("IP address       HW type     Flags       HW address            Mask     Device")) {
                    arpTableRows.add(line.substring(0, line.indexOf(" ")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!arpTableRows.isEmpty()) {
//            System.out.println("ARP TABLE SERVER: " + arpTableRows.get(0));
            try {
                return InetAddress.getByName(arpTableRows.get(0));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        return inetAddressFromInt(NetworkHelper.getWifiManager().getDhcpInfo().gateway);
    }
}
