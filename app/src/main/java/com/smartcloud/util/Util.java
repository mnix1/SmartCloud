package com.smartcloud.util;

import android.util.Base64;
import android.util.Log;

import com.smartcloud.database.ServerDatabase;
import com.smartcloud.holder.MachineHolder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

public class Util {
    public static final String[] UNITS = new String[]{"B", "KB", "MB", "GB", "TB", "PB"};

    public static String serializeToString(Object object) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
            return new String(Base64.encode(byteArrayOutputStream.toByteArray(), Base64.NO_WRAP));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object deserializeFromString(String data) {
        try {
            return new ObjectInputStream(new ByteArrayInputStream(Base64.decode(data.getBytes(), Base64.NO_WRAP))).readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String sizeToReadableUnit(Long sizeBytes) {
        int index = 0;
        if (sizeBytes == null) {
            return "0 " + UNITS[index];
        }
        double size = sizeBytes;
        while (size > 1024 && index < UNITS.length) {
            index++;
            size /= 1024;
        }
        return round(size, 2) + " " + UNITS[index];
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static void log() {
        StringBuilder stringBuilder = new StringBuilder("freeSpace: " + ServerDatabase.instance.selectFreeSpace());
        stringBuilder.append("\n");
        for (MachineHolder machine : ServerDatabase.instance.selectMachine()) {
            stringBuilder.append(machine.toString());
            stringBuilder.append("\n");
        }
        Log.v("SMART CLOUD", stringBuilder.toString());
    }


    public static void readFromFileWriteToStream(File file, Object output, int bufferSize) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[bufferSize];
            int read;
            if (output instanceof PrintWriter) {
                PrintWriter printWriter = (PrintWriter) output;
                while ((read = fileInputStream.read(buffer)) >= 0) {
                    if (read == bufferSize) {
                        printWriter.println(new String(Base64.encode(buffer, Base64.NO_WRAP)));
                    } else {
                        printWriter.println(new String(Base64.encode(Arrays.copyOf(buffer, read), Base64.NO_WRAP)));
                    }
                }
            } else {
                OutputStream outputStream = (OutputStream) output;
                while ((read = fileInputStream.read(buffer)) >= 0) {
                    outputStream.write(buffer, 0, read);
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
