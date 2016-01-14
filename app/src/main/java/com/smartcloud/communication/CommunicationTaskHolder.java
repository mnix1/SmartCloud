package com.smartcloud.communication;

import com.smartcloud.constant.MethodType;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class CommunicationTaskHolder {
    public CommunicationTask task;
    public Object[] args;

    public CommunicationTaskHolder(CommunicationTask task, Object... args) {
        this.task = task;
        this.args = args;
    }

    public CommunicationTaskHolder(CommunicationTask task) {
        this.task = task;
    }

    public boolean findAction(MethodType methodType, Object receiver) {
        if (methodType.equals(MethodType.READ)) {
            return invokeMethod(task.methodNameRead, receiver, args);
        } else if (methodType.equals(MethodType.WRITE)) {
            return invokeMethod(task.methodNameWrite, receiver, args);
        }
        return false;
    }

    public static boolean findAction(CommunicationTaskHolder taskHolder, Object receiver) {
        return invokeMethod(taskHolder.task.methodNameWrite, receiver);
    }

    public static boolean findAction(String taskName, Object receiver) {
        CommunicationTask task = null;
        try {
            task = Enum.valueOf(CommunicationTask.class, taskName);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }
        return invokeMethod(task.methodNameRead, receiver);
    }

    public static boolean invokeMethod(String methodName, Object receiver, Object... args) {
        List<Class> parameterTypes = new ArrayList<>(args != null ? args.length : 0);
        if (args != null && args.length > 0) {
            for (Object object : args) {
                parameterTypes.add(object.getClass());
            }
        }
        try {
            receiver.getClass().getMethod(methodName, parameterTypes.toArray(new Class[parameterTypes.size()])).invoke(receiver, args);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return true;
    }
}
