package com.smartcloud;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.smartcloud.database.ClientDatabase;
import com.smartcloud.database.Database;
import com.smartcloud.file.ConfigManager;
import com.smartcloud.file.FileManager;
import com.smartcloud.network.NetworkManager;

public class MainActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (NetworkManager.instance == null) {
            FileManager.setDirs();
            ClientDatabase.init(getBaseContext());
            ConfigManager.importMe();
            NetworkManager.init(getBaseContext());
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
