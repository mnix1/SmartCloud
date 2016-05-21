package com.smartcloud.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.smartcloud.MainActivity;
import com.smartcloud.holder.MachineHolder;
import com.smartcloud.holder.SegmentHolder;

public class ClientDatabase extends Database {
    public static final String DB_NAME_CLIENT = "SmartCloudClientDb";
    public static ClientDatabase instance;

    private ClientDatabase(SQLiteDatabase database) {
        super(database);
    }

    public static String createTableMachineHolderSQL() {
        StringBuilder stringBuilder = new StringBuilder(CREATE_STM);
        stringBuilder.append(MachineHolder.TABLE_NAME);
        stringBuilder.append(MachineHolder.TABLE_COLUMNS_CLIENT);
        stringBuilder.append(END_STM);
        return stringBuilder.toString();
    }

    public static String createTableSegmentHolderSQL() {
        StringBuilder stringBuilder = new StringBuilder(CREATE_STM);
        stringBuilder.append(SegmentHolder.TABLE_NAME);
        stringBuilder.append(SegmentHolder.TABLE_COLUMNS_CLIENT);
        stringBuilder.append(END_STM);
        return stringBuilder.toString();
    }

    public static ClientDatabase init() {
        SQLiteDatabase database = MainActivity.currentContext.openOrCreateDatabase(DB_NAME_CLIENT, Context.MODE_PRIVATE, null);
        database.execSQL(createTableMachineHolderSQL());
        database.execSQL(createTableSegmentHolderSQL());
        System.out.println("DB CREATED " + database.getPath());
        instance = new ClientDatabase(database);
        return instance;
    }

    public boolean insertMachine(String id) {
        ContentValues args = new ContentValues();
        args.put("id", id);
        long result = mDatabase.insert(MachineHolder.TABLE_NAME, null, args);
        if (result == -1) {
            return false;
        }
        return true;
    }

    public boolean insertMachine(MachineHolder machineHolder) {
        ContentValues args = new ContentValues();
        args.put("id", machineHolder.getId());
        args.put("machineRole", machineHolder.getMachineRole().toString());
        args.put("active", machineHolder.getActive());
        long result = mDatabase.insert(MachineHolder.TABLE_NAME, null, args);
        if (result == -1) {
            return false;
        }
        return true;
    }


    public boolean insertSegment(SegmentHolder segmentHolder) {
        ContentValues args = new ContentValues();
        args.put("id", segmentHolder.getId());
        args.put("path", segmentHolder.getPath());
        long result = mDatabase.insert(SegmentHolder.TABLE_NAME, null, args);
        if (result == -1) {
            return false;
        }
        return true;
    }

    public MachineHolder selectMachine() {
        Cursor cursor = mDatabase.query(MachineHolder.TABLE_NAME, null, null, null, null, null, null);
        if (!cursor.moveToFirst()) {
            return null;
        }
        MachineHolder machineHolder = new MachineHolder(cursor.getString(cursor.getColumnIndex("id")), cursor.getString(cursor.getColumnIndex("machineRole")));
        cursor.close();
        return machineHolder;
    }


    public SegmentHolder selectSegment(Long id) {
        Cursor cursor = mDatabase.rawQuery("SELECT path FROM " + SegmentHolder.TABLE_NAME + " WHERE id = " + id, null);
        //mDatabase.query(SegmentHolder.TABLE_NAME, new String[]{"path"}, "id = ?s", new String[]{id.toString()}, null, null, null);
        if (!cursor.moveToFirst()) {
            return null;
        }
        String path = cursor.getString(cursor.getColumnIndex("path"));
        cursor.close();
        return new SegmentHolder(id, path);
    }

    public int deleteMachine(MachineHolder machineHolder) {
        return mDatabase.delete(MachineHolder.TABLE_NAME, "id = ?", new String[]{machineHolder.getId()});
    }

    public int deleteSegment(SegmentHolder segmentHolder) {
        return mDatabase.delete(SegmentHolder.TABLE_NAME, "id = ?", new String[]{segmentHolder.getId().toString()});
    }

    public boolean updateMachine(MachineHolder machineHolder) {
        deleteMachine(machineHolder);
        return insertMachine(machineHolder);
    }

}
