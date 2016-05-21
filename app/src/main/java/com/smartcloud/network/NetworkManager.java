package com.smartcloud.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.smartcloud.MainActivity;
import com.smartcloud.connection.ConnectionManager;
import com.smartcloud.constant.MachineRole;
import com.smartcloud.util.NetworkHelper;

import java.util.ArrayList;
import java.util.List;

public class NetworkManager {
    public static NetworkManager instance;
    public static boolean createAp = true;

    private final WifiManager mWifiManager;
    private final WifiReceiver mWifiReceiver;

    private int mWifiState;
    private List<ScanResult> mScanResults;
    private NetworkInfo mWifiNetworkInfo;
    private WifiApControl mWifiApControl;

    private List<NetworkInitializedListener> mNetworkInitializedListeners;

    private NetworkManager() {
        this.mNetworkInitializedListeners = new ArrayList<>();
        this.mWifiManager = NetworkHelper.getWifiManager();
        this.mWifiReceiver = new WifiReceiver();
        this.mWifiApControl = WifiApControl.getApControl(mWifiManager);
    }

    public static void init(){
        instance = new NetworkManager();
        instance.addNetworkInitializerListener(new ConnectionManager());
        instance.setNetworkDisable();
        instance.registerReceiver();
        instance.setWifiEnabled();
        instance.startScan();
    }

    public void setNetworkDisable() {
        setWifiDisable();
        disableAp();
    }

    public void setWifiEnabled() {
//        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
//        }
    }

    public void setWifiDisable() {
//        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
//        }
    }

    public void disableAp() {
        if (mWifiApControl == null) {
            return;
        }
        mWifiApControl.setWifiApEnabled(NetworkHelper.getWifiConfiguration(), false);
    }

    public void startScan(){
        mWifiManager.startScan();
    }

    public void registerReceiver() {
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        MainActivity.currentContext.registerReceiver(mWifiReceiver, mIntentFilter);
    }

    public void unregisterReceiver() {
        MainActivity.currentContext.unregisterReceiver(mWifiReceiver);
    }

    private Boolean isWifiApAvailable() {
        if (mScanResults == null) {
            return null;
        }
        for (ScanResult scanResult : mScanResults) {
            if (NetworkHelper.SSID.equals(scanResult.SSID)) {
                return true;
            }
        }
        return false;
    }

    public void notifyWifiState() {
        logNetworkInfo();
        Boolean isWifiApAvailable = isWifiApAvailable();
        if(!mWifiManager.isWifiEnabled() && (isWifiApAvailable != null && isWifiApAvailable)){
            setWifiEnabled();
        }
        if (!mWifiManager.isWifiEnabled() || (mWifiNetworkInfo != null && mWifiNetworkInfo.getState() == NetworkInfo.State.CONNECTING)) {
            Log.v("notifyWifiState", "return");
            return;
        }
        if (mWifiNetworkInfo != null && mWifiNetworkInfo.isConnected()) {
            WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
            if (NetworkHelper.convertToQuotedString(NetworkHelper.SSID).equals(wifiInfo.getSSID()) || NetworkHelper.SSID.equals(wifiInfo.getSSID())) {
                notifyInitialized(MachineRole.SLAVE);
            } else {
                disconnect();
                startScan();
            }
        } else {
            if (isWifiApAvailable != null) {
                if (isWifiApAvailable) {
                    connect();
                } else {
                    if(createAp){
                        createWifiAp();
                        notifyInitialized(MachineRole.MASTER);
                    } else {
                        startScan();
                    }
                }
            }
        }
    }

    public boolean createWifiAp() {
        setWifiDisable();
        Log.v("createWifiAp", "createWifiAp");
        if (mWifiApControl == null) {
            return false;
        }
        return mWifiApControl.setWifiApEnabled(NetworkHelper.getWifiConfiguration(), true);
    }

    public boolean disconnect() {
        Log.v("disconnect", "disconnect");
        if (!mWifiManager.isWifiEnabled()) {
            return false;
        }
        WifiInfo curWifi = mWifiManager.getConnectionInfo();
        if (curWifi == null) {
            return false;
        }
        int curNetworkId = curWifi.getNetworkId();
        mWifiManager.removeNetwork(curNetworkId);
        mWifiManager.saveConfiguration();

        // remove other saved networks
        List<WifiConfiguration> netConfList = mWifiManager.getConfiguredNetworks();
        if (netConfList != null) {
            Log.v("disconnect", "remove configured network ids");
            for (int i = 0; i < netConfList.size(); i++) {
                WifiConfiguration conf = netConfList.get(i);
                mWifiManager.removeNetwork(conf.networkId);
            }
        }
        mWifiManager.saveConfiguration();
        return true;
    }

    public boolean connect() {
        Log.v("connect", "connect");
        if (!mWifiManager.isWifiEnabled()) {
            return false;
        }
        for (ScanResult scanResult : mScanResults) {
            if (NetworkHelper.SSID.equals(scanResult.SSID)) {
                WifiConfiguration config = new WifiConfiguration();
                config.SSID = NetworkHelper.convertToQuotedString(scanResult.SSID);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                int networkId = mWifiManager.addNetwork(config);
                mWifiManager.enableNetwork(networkId, true);
                mWifiManager.saveConfiguration();
                mWifiManager.reconnect();
                break;
            }
        }
        List<WifiConfiguration> netConfList = mWifiManager.getConfiguredNetworks();
        if (netConfList.size() <= 0) {
            return false;
        }
        return true;
    }

    private class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context c, Intent intent) {
            String action = intent.getAction();
            Log.v("WifiReceiver", "onReceive() is calleld with " + intent + " - " + action);
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                mScanResults = mWifiManager.getScanResults();
                notifyWifiState();
            } else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                mWifiNetworkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                notifyWifiState();
            } else if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                mWifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                notifyWifiState();
            } else {
                return;
            }
        }
    }

    public void addNetworkInitializerListener(NetworkInitializedListener networkInitializedListener) {
        mNetworkInitializedListeners.add(networkInitializedListener);
    }

    public void removeNetworkInitializerListener(NetworkInitializedListener networkInitializedListener) {
        mNetworkInitializedListeners.remove(networkInitializedListener);
    }

    public void notifyInitialized(MachineRole machineRole) {
        Log.v("notifyInitialized", "notifyInitialized");
        unregisterReceiver();
        for (NetworkInitializedListener networkInitializedListener : mNetworkInitializedListeners) {
            networkInitializedListener.initialized(machineRole);
        }
    }

    public void logNetworkInfo() {
        if (mWifiNetworkInfo != null) {
            Log.v("mWifiNetworkInfo", "mWifiNetworkInfo: " + mWifiNetworkInfo.toString());
            if (mWifiNetworkInfo.isConnected()) {
                Log.v("WifiInfo", "WifiInfo: " + mWifiManager.getConnectionInfo().toString());
            }
        }
        if (mScanResults != null) {
            for (int i = 0; i < mScanResults.size(); i++) {
                Log.v("mScanResults", "mScanResults: " + mScanResults.get(i).toString());
            }
        }
        Log.v("mWifiState", "mWifiState: " + mWifiState);
    }
}

