package com.smartcloud;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.smartcloud.database.ClientDatabase;
import com.smartcloud.holder.MachineHolder;
import com.smartcloud.network.NetworkManager;
import com.smartcloud.util.FileManager;

public class MainActivity extends AppCompatActivity {

    public static Context currentContext = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (NetworkManager.instance == null) {
            currentContext = getBaseContext();
            FileManager.setDirs();
            ClientDatabase.init();
            MachineHolder.setMyId();
            NetworkManager.init();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
