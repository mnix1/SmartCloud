package com.smartcloud.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class Database {
    protected Context mContext;
    protected SQLiteDatabase mDatabase;

    protected final static String CREATE_STM = "CREATE TABLE IF NOT EXISTS ";
    protected final static String END_STM = ";\n";

    protected Database(Context context, SQLiteDatabase database) {
        this.mContext = context;
        this.mDatabase = database;
    }

    public SQLiteDatabase getDatabase() {
        return mDatabase;
    }
}
