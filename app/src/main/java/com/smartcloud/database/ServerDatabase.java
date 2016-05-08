package com.smartcloud.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.smartcloud.file.FileManager;
import com.smartcloud.holder.FileHolder;
import com.smartcloud.holder.SegmentHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ServerDatabase extends Database {
    public static final String DB_NAME_SERVER = "SmartCloudServerDb";
    public static ServerDatabase instance;

    private ServerDatabase(Context context, SQLiteDatabase database) {
        super(context, database);
    }

    private static String createTableFileHolderSQL() {
        StringBuilder stringBuilder = new StringBuilder(CREATE_STM);
        stringBuilder.append(FileHolder.TABLE_NAME);
        stringBuilder.append(FileHolder.TABLE_COLUMNS_SERVER);
        stringBuilder.append(END_STM);
        return stringBuilder.toString();
    }
    private static String createTableSegmentHolderSQL() {
        StringBuilder stringBuilder = new StringBuilder(CREATE_STM);
        stringBuilder.append(SegmentHolder.TABLE_NAME);
        stringBuilder.append(SegmentHolder.TABLE_COLUMNS_SERVER);
        stringBuilder.append(END_STM);
        return stringBuilder.toString();
    }

    public static ServerDatabase init(Context context) {
        SQLiteDatabase database = context.openOrCreateDatabase(DB_NAME_SERVER, Context.MODE_PRIVATE, null);
        database.execSQL(createTableFileHolderSQL());
        database.execSQL(createTableSegmentHolderSQL());
        System.out.println("DB CREATED " + database.getPath());
        instance = new ServerDatabase(context, database);
        return instance;
    }

    public boolean insertFile(FileHolder fileHolder) {
        ContentValues args = new ContentValues();
        args.put("name", fileHolder.getName());
        args.put("size", fileHolder.getSize());
        long result = mDatabase.insert(FileHolder.TABLE_NAME, null, args);
        if (result == -1) {
            return false;
        }
        fileHolder.setId(result);
        return true;
    }

    public FileHolder selectFile(Long id) {
        Cursor cursor = mDatabase.rawQuery("SELECT id, name, size FROM " + FileHolder.TABLE_NAME + " WHERE id = " + id, null);
        cursor.moveToFirst();
        if (!cursor.moveToFirst()) {
            return null;
        }
        FileHolder fileHolder = new FileHolder(id, cursor.getString(cursor.getColumnIndex("name")), cursor.getLong(cursor.getColumnIndex("size")));
        cursor.close();
        fileHolder.setFile(new File(FileManager.storageDir + "/" + fileHolder.getName()));
        return fileHolder;
    }

    public List<FileHolder> selectFile() {
        Cursor cursor = mDatabase.query(FileHolder.TABLE_NAME, new String[]{"id", "name", "size"}, null, null, null, null, null);
        List<FileHolder> files = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            files.add(new FileHolder(cursor.getLong(cursor.getColumnIndex("id")), cursor.getString(cursor.getColumnIndex("name")), cursor.getLong(cursor.getColumnIndex("size"))));
        }
        cursor.close();
        return files;
    }

    public boolean insertSegment(SegmentHolder segmentHolder) {
        ContentValues args = new ContentValues();
        args.put("fileId", segmentHolder.getFileId());
        args.put("machineId", segmentHolder.getMachineId());
        args.put("byteFrom", segmentHolder.getByteFrom());
        args.put("byteTo", segmentHolder.getByteTo());
        long result = mDatabase.insert(SegmentHolder.TABLE_NAME, null, args);
        if (result == -1) {
            return false;
        }
        segmentHolder.setId(result);
        return true;
    }

//    public List<SegmentHolder> selectSegment(Long fileId, String machineId) {
//        Cursor cursor = mDatabase.rawQuery("SELECT id, fileId, machineId, byteFrom, byteTo FROM " + SegmentHolder.TABLE_NAME + " WHERE fileId = " + fileId + " AND machineId = " + machineId, null);
//        List<SegmentHolder> segments = new ArrayList<>(cursor.getCount());
//        while (cursor.moveToNext()) {
//            segments.add(new SegmentHolder(cursor.getLong(cursor.getColumnIndex("id")),
//                    cursor.getLong(cursor.getColumnIndex("fileId")),
//                    cursor.getString(cursor.getColumnIndex("machineId")),
//                    cursor.getLong(cursor.getColumnIndex("byteFrom")),
//                    cursor.getLong(cursor.getColumnIndex("byteTo"))));
//        }
//        cursor.close();
//        return segments;
//    }

    public List<SegmentHolder> selectSegment(Long fileId) {
        Cursor cursor = fileId != null ?
                mDatabase.rawQuery("SELECT id, fileId, machineId, byteFrom, byteTo FROM " + SegmentHolder.TABLE_NAME + " WHERE fileId = " + fileId, null):
//                mDatabase.query(SegmentHolder.TABLE_NAME, new String[]{"id", "fileId", "machineId", "offset", "size"},
//                        "fileId = '?s'", new String[]{fileId.toString()}, null, null, null) :
                mDatabase.query(SegmentHolder.TABLE_NAME, new String[]{"id", "fileId", "machineId", "byteFrom", "byteTo"},
                        null, null, null, null, null);
        List<SegmentHolder> segments = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            segments.add(new SegmentHolder(cursor.getLong(cursor.getColumnIndex("id")),
                    cursor.getLong(cursor.getColumnIndex("fileId")),
                    cursor.getString(cursor.getColumnIndex("machineId")),
                    cursor.getLong(cursor.getColumnIndex("byteFrom")),
                    cursor.getLong(cursor.getColumnIndex("byteTo"))));
        }
        cursor.close();
        return segments;
    }

    public int deleteFile(Long fileId) {
        return mDatabase.delete(FileHolder.TABLE_NAME, "id = ?", new String[]{fileId.toString()});
//        mDatabase.rawQuery("DELETE FROM " + FileHolder.TABLE_NAME + " WHERE id = " + fileId, null);
    }

    public int deleteSegments(Long fileId) {
//        mDatabase.rawQuery("DELETE FROM " + SegmentHolder.TABLE_NAME + " WHERE fileId = " + fileId, null);
        return mDatabase.delete(SegmentHolder.TABLE_NAME, "fileId = ?", new String[]{fileId.toString()});
    }
}
