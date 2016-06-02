package com.smartcloud.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.smartcloud.MainActivity;
import com.smartcloud.constant.MachineRole;
import com.smartcloud.holder.FileHolder;
import com.smartcloud.holder.MachineHolder;
import com.smartcloud.holder.SegmentHolder;
import com.smartcloud.util.FileManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ServerDatabase extends Database {
    public static final String DB_NAME_SERVER = "SmartCloudServerDb";
    public static ServerDatabase instance;

    private ServerDatabase(SQLiteDatabase database) {
        super(database);
    }

    private static String createTableMachineHolderSQL() {
        StringBuilder stringBuilder = new StringBuilder(CREATE_STM);
        stringBuilder.append(MachineHolder.TABLE_NAME);
        stringBuilder.append(MachineHolder.TABLE_COLUMNS_SERVER);
        stringBuilder.append(END_STM);
        return stringBuilder.toString();
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

    public static ServerDatabase init() {
        SQLiteDatabase database = MainActivity.currentContext.openOrCreateDatabase(DB_NAME_SERVER, Context.MODE_PRIVATE, null);
        database.execSQL(createTableMachineHolderSQL());
        database.execSQL(createTableFileHolderSQL());
        database.execSQL(createTableSegmentHolderSQL());
        System.out.println("DB CREATED " + database.getPath());
        instance = new ServerDatabase(database);
        return instance;
    }


    public boolean insertMachine(MachineHolder machineHolder) {
        ContentValues args = new ContentValues();
        args.put("id", machineHolder.getId());
        args.put("machineRole", machineHolder.getMachineRole().toString());
        args.put("address", machineHolder.getAddress());
        args.put("freeSpace", machineHolder.getFreeSpace());
        args.put("totalSpace", machineHolder.getTotalSpace());
        args.put("active", machineHolder.getActive());
        args.put("lastContact", machineHolder.getLastContact().getTime());
        long result = mDatabase.insert(MachineHolder.TABLE_NAME, null, args);
        if (result == -1) {
            return false;
        }
        return true;
    }

    public boolean insertFile(FileHolder fileHolder) {
        ContentValues args = new ContentValues();
        if (fileHolder.getId() != null) {
            args.put("id", fileHolder.getId());
        }
        args.put("name", fileHolder.getName());
        args.put("size", fileHolder.getSize());
        long result = mDatabase.insert(FileHolder.TABLE_NAME, null, args);
        if (result == -1) {
            return false;
        }
        fileHolder.setId(result);
        return true;
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

    public MachineHolder selectMachine(String id) {
        Cursor cursor = mDatabase.rawQuery("SELECT id, machineRole, address, freeSpace, totalSpace, active, lastContact FROM " + MachineHolder.TABLE_NAME + " WHERE id = '" + id + "'", null);
        cursor.moveToFirst();
        if (!cursor.moveToFirst()) {
            return null;
        }
        MachineRole machineRole = Enum.valueOf(MachineRole.class, cursor.getString(cursor.getColumnIndex("machineRole")));
        MachineHolder machineHolder = new MachineHolder(id, machineRole, cursor.getString(cursor.getColumnIndex("address")),
                cursor.getLong(cursor.getColumnIndex("freeSpace")), cursor.getLong(cursor.getColumnIndex("totalSpace")), cursor.getInt(cursor.getColumnIndex("active")),
                new Date(cursor.getLong(cursor.getColumnIndex("lastContact"))));
        cursor.close();
        return machineHolder;
    }

    public List<MachineHolder> selectMachine() {
        return selectMachine((Boolean) null);
    }

    public List<MachineHolder> selectMachine(Boolean active) {
        Cursor cursor = active == null ? mDatabase.query(MachineHolder.TABLE_NAME, new String[]{"id", "machineRole", "address", "freeSpace", "totalSpace", "active", "lastContact"},
                null, null, null, null, null) :
                (mDatabase.rawQuery("SELECT id, machineRole, address, freeSpace, totalSpace, active, lastContact FROM " + MachineHolder.TABLE_NAME +
                        " WHERE active = " + (active ? 1 : 0), null));
        List<MachineHolder> machines = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            MachineRole machineRole = Enum.valueOf(MachineRole.class, cursor.getString(cursor.getColumnIndex("machineRole")));
            machines.add(new MachineHolder(cursor.getString(cursor.getColumnIndex("id")), machineRole, cursor.getString(cursor.getColumnIndex("address")),
                    cursor.getLong(cursor.getColumnIndex("freeSpace")), cursor.getLong(cursor.getColumnIndex("totalSpace")), cursor.getInt(cursor.getColumnIndex("active")),
                    new Date(cursor.getLong(cursor.getColumnIndex("lastContact")))));
        }
        cursor.close();
        return machines;
    }

//    public List<MachineHolder> selectMachine(Long fileId) {
//        Cursor cursor = mDatabase.rawQuery("SELECT m.id as id, m.machineRole as machineRole, m.address as address , m.active as active FROM " + MachineHolder.TABLE_NAME + " m, " +
//                SegmentHolder.TABLE_NAME + " s WHERE m.id = s.machineId AND s.fileId = " + fileId, null);
//        List<MachineHolder> machines = new ArrayList<>(cursor.getCount());
//        while (cursor.moveToNext()) {
//            MachineRole machineRole = Enum.valueOf(MachineRole.class, cursor.getString(cursor.getColumnIndex("machineRole")));
//            machines.add(new MachineHolder(cursor.getString(cursor.getColumnIndex("id")), machineRole, cursor.getString(cursor.getColumnIndex("address")),
//                    null, null, cursor.getInt(cursor.getColumnIndex("active")), null));
//        }
//        cursor.close();
//        return machines;
//    }

    public Long selectFreeSpace() {
        Cursor cursor = mDatabase.rawQuery("SELECT SUM(freeSpace) as freeSpace FROM " + MachineHolder.TABLE_NAME + " WHERE active = 1", null);
        cursor.moveToFirst();
        if (!cursor.moveToFirst()) {
            return null;
        }
        Long freeSpace = cursor.getLong(cursor.getColumnIndex("freeSpace"));
        cursor.close();
        return freeSpace;
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

    public List<FileHolder> selectFile(Boolean active) {
        Cursor cursor = null;
        if (active == null) {
            cursor = mDatabase.query(FileHolder.TABLE_NAME, new String[]{"id", "name", "size"}, null, null, null, null, null);
        } else if (active) {
            cursor = mDatabase.rawQuery("SELECT o.id as id, o.name as name, o.size as size FROM " +
                    FileHolder.TABLE_NAME + " o WHERE o.id NOT IN (SELECT DISTINCT f.id FROM " +
                    FileHolder.TABLE_NAME + " f, " + MachineHolder.TABLE_NAME + " m, " + SegmentHolder.TABLE_NAME +
                    " s WHERE f.id = s.fileId AND ((s.machineId = m.id AND m.active = 0) OR (s.machineId NOT IN (SELECT DISTINCT id FROM " + MachineHolder.TABLE_NAME + "))))", null);
        } else {
            cursor = mDatabase.rawQuery("SELECT DISTINCT f.id as id, f.name as name, f.size as size FROM " + FileHolder.TABLE_NAME + " f, " +
                    MachineHolder.TABLE_NAME + " m, " + SegmentHolder.TABLE_NAME +
                    " s WHERE f.id = s.fileId AND ((s.machineId = m.id AND m.active = 0) OR (s.machineId NOT IN (SELECT DISTINCT id FROM " + MachineHolder.TABLE_NAME + ")))", null);
        }
        List<FileHolder> files = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            files.add(new FileHolder(cursor.getLong(cursor.getColumnIndex("id")), cursor.getString(cursor.getColumnIndex("name")), cursor.getLong(cursor.getColumnIndex("size"))));
        }
        cursor.close();
        return files;
    }

    public List<FileHolder> selectFile() {
        return selectFile((Boolean) null);
    }

    public List<SegmentHolder> selectSegment(Long fileId) {
        Cursor cursor = fileId == null ? mDatabase.query(SegmentHolder.TABLE_NAME, new String[]{"id", "fileId", "machineId", "byteFrom", "byteTo"},
                null, null, null, null, null) :
                mDatabase.rawQuery("SELECT id, fileId, machineId, byteFrom, byteTo FROM " + SegmentHolder.TABLE_NAME + " WHERE fileId = " + fileId + " ORDER BY byteFrom", null);
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

    public int deleteMachine(String machineId) {
        return machineId == null ? mDatabase.delete(MachineHolder.TABLE_NAME, null, null) :
                mDatabase.delete(MachineHolder.TABLE_NAME, "id = ?", new String[]{machineId});
    }

    public int deleteFile(Long fileId) {
        return mDatabase.delete(FileHolder.TABLE_NAME, "id = ?", new String[]{fileId.toString()});
    }

    public int deleteSegments(Long fileId) {
        return mDatabase.delete(SegmentHolder.TABLE_NAME, "fileId = ?", new String[]{fileId.toString()});
    }

    public boolean updateMachine(MachineHolder machineHolder) {
        mDatabase.beginTransaction();
        int deleted = deleteMachine(machineHolder.getId());
        boolean result = insertMachine(machineHolder);
        mDatabase.setTransactionSuccessful();
        mDatabase.endTransaction();
        return result;
    }

    public void updateMachines(boolean active) {
        List<MachineHolder> machines = selectMachine(!active);
        mDatabase.delete(MachineHolder.TABLE_NAME, "active = ?", new String[]{active ? "0" : "1"});
        for (MachineHolder machineHolder : machines) {
            insertMachine(machineHolder);
        }
    }

    public boolean updateFile(FileHolder fileHolder) {
        mDatabase.beginTransaction();
        deleteFile(fileHolder.getId());
        boolean result = insertFile(fileHolder);
        mDatabase.setTransactionSuccessful();
        mDatabase.endTransaction();
        return result;
    }
}
