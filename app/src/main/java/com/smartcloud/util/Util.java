package com.smartcloud.util;

import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;

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
}
