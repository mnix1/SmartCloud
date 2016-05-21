package com.smartcloud.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class Database {
    protected SQLiteDatabase mDatabase;

    protected final static String CREATE_STM = "CREATE TABLE IF NOT EXISTS ";
    protected final static String END_STM = ";\n";

    protected Database( SQLiteDatabase database) {
        this.mDatabase = database;
    }

    public SQLiteDatabase getDatabase() {
        return mDatabase;
    }
}
