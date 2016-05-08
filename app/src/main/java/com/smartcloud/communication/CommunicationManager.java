package com.smartcloud.communication;

import android.content.Context;
import android.util.Base64;

import com.smartcloud.database.ClientDatabase;
import com.smartcloud.file.FileManager;
import com.smartcloud.holder.CloudHolder;
import com.smartcloud.holder.FileHolder;
import com.smartcloud.holder.MachineHolder;
import com.smartcloud.holder.SegmentHolder;
import com.smartcloud.util.Util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class CommunicationManager implements Runnable {
    public final List<CommunicationTaskHolder> tasks = new ArrayList<>();
    protected Context mContext;
    protected Socket mSocket;
    protected BufferedReader mInput;
    protected PrintWriter mOutput;
    Thread writeThread;
    Thread readThread;

    protected CommunicationManager(Socket socket, Context context) {
        this.mSocket = socket;
        this.mContext = context;
        try {
            this.mInput = new BufferedReader(new InputStreamReader(this.mSocket.getInputStream()));
            this.mOutput = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream())), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendObject(Object object) {
        try {
            String message = Util.serializeToString(object);
            mOutput.println(message.length());
            mOutput.print(message);
            mOutput.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Object receiveObject() {
        try {
            int messageLength = Integer.parseInt(mInput.readLine());
            char[] chars = new char[messageLength];
            int read = 0;
            while (read < messageLength) {
                read += mInput.read(chars, read, messageLength);
            }
            return Util.deserializeFromString(new String(chars));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Socket getSocket() {
        return mSocket;
    }

    public void writeClientMachineHolder() {
        mOutput.println(CommunicationTask.GET_CLIENT_MACHINE_HOLDER);
        sendObject(MachineHolder.ME);
    }

    public void readClientMachineHolder() {
        MachineHolder machine = (MachineHolder) receiveObject();
        machine.setCommunicationManager(this);
        CloudHolder.updateMachines(machine);
        CloudHolder.log();
    }


    public void writeFile(Long segmentId, FileHolder fileHolder) {
        File file = fileHolder.getFile();
        int fileSize = (int) file.length();
        System.out.println("writeFile start fileSize " + fileSize + " path " + file);
        FileInputStream fileInputStream = null;
        mOutput.println(fileHolder.getId());
        mOutput.println(segmentId);
        mOutput.println(fileSize);
        try {
            fileInputStream = new FileInputStream(file);
            int bufferSize = mSocket.getSendBufferSize();
            byte[] buffer = new byte[bufferSize];
            int read;
            while ((read = fileInputStream.read(buffer)) >= 0) {
                if (read == bufferSize) {
                    mOutput.println(new String(Base64.encode(buffer, Base64.NO_WRAP)));
                } else {
                    mOutput.println(new String(Base64.encode(Arrays.copyOf(buffer, read), Base64.NO_WRAP)));
                }
                System.out.println("writeFile read " + read);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SocketException e) {
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
        System.out.println("writeFile end");
    }

    public File readFile() {
        Long fileId = null;
        Long segmentId = null;
        File file = null;
        FileOutputStream fileOutputStream = null;
        try {
            fileId = Long.parseLong(mInput.readLine());
            segmentId = Long.parseLong(mInput.readLine());
            file = new File(FileManager.storageDir + "/" + fileId + "^_^" + segmentId);
            int fileSize = Integer.parseInt(mInput.readLine());
            System.out.println("readFile start fileSize " + fileSize + " path " + file);
            fileOutputStream = new FileOutputStream(file);
            int totalRead = 0;
            while (totalRead < fileSize) {
                byte[] bytes = Base64.decode(mInput.readLine().getBytes(), Base64.NO_WRAP);
                fileOutputStream.write(bytes);
                fileOutputStream.flush();
                totalRead += bytes.length;
                System.out.println("readFile read " + bytes.length + " totalRead " + totalRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fileOutputStream.close();
                CloudHolder.updateFilesToManage(fileId, segmentId, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("readFile end");
        return file;
    }


    public void writeSegment(FileHolder fileHolder, SegmentHolder segmentHolder) {
        System.out.println("writeSegment start from " + segmentHolder.getByteFrom() + " to " + segmentHolder.getByteTo());
        FileInputStream fileInputStream = null;
        mOutput.println(CommunicationTask.SEND_SEGMENT);
        long totalToRead = segmentHolder.getByteTo() - segmentHolder.getByteFrom() + 1;
        mOutput.println(segmentHolder.getId());
        mOutput.println(totalToRead);
        try {
            fileInputStream = new FileInputStream(fileHolder.getFile());
            fileInputStream.skip(segmentHolder.getByteFrom());
            int bufferSize = mSocket.getSendBufferSize();
            byte[] buffer = new byte[bufferSize];
            int totalRead = 0;
            int read = 0;
            while ((read = fileInputStream.read(buffer, 0, Math.min((int) totalToRead - totalRead, bufferSize))) > 0 && totalRead < totalToRead) {
                if (read == bufferSize) {
                    mOutput.println(new String(Base64.encode(buffer, Base64.NO_WRAP)));
                } else {
                    mOutput.println(new String(Base64.encode(Arrays.copyOf(buffer, read), Base64.NO_WRAP)));
                }
                System.out.println("writeSegment read " + read);
                totalRead += read;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fileInputStream.close();
                segmentHolder.setReady(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("writeSegment end");
    }

    public File readSegment() {
        File file = null;
        FileOutputStream fileOutputStream = null;
        try {
            Long segmentId = Long.parseLong(mInput.readLine());
            int fileSize = Integer.parseInt(mInput.readLine());
            SegmentHolder segmentHolder = new SegmentHolder(segmentId, FileManager.storageDir + "/" + segmentId);
            System.out.println("ClientDatabase.instance.insertSegment(segmentHolder);" + ClientDatabase.instance.insertSegment(segmentHolder));
            file = new File(segmentHolder.getPath());
            System.out.println("readSegment start fileSize " + fileSize + " segment " + segmentHolder);
            fileOutputStream = new FileOutputStream(file);
            int totalRead = 0;
            while (totalRead < fileSize) {
                byte[] bytes = Base64.decode(mInput.readLine().getBytes(), Base64.NO_WRAP);
                fileOutputStream.write(bytes);
                fileOutputStream.flush();
                totalRead += bytes.length;
                System.out.println("readSegment read " + bytes.length + " totalRead " + totalRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("readSegment end");
        return file;
    }


    public void requestForSendSegment(Long fileId, Long segmentId) {
        mOutput.println(CommunicationTask.REQUEST_FOR_SEND_SEGMENT);
        mOutput.println(fileId);
        mOutput.println(segmentId);
    }

    public void responseForRequestForSendSegment() {
        try {
            Long fileId = Long.parseLong(mInput.readLine());
            Long segmentId = Long.parseLong(mInput.readLine());
            SegmentHolder segmentHolder = ClientDatabase.instance.selectSegment(segmentId);
            File file = new File(segmentHolder.getPath());
            FileHolder fileHolder = new FileHolder(fileId, file);
            mOutput.println(CommunicationTask.RESPONSE_FOR_SEND_SEGMENT);
            writeFile(segmentId, fileHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiveSegment() {
        readFile();
    }

    public void requestForDeleteSegment(Long segmentId) {
        mOutput.println(CommunicationTask.REQUEST_FOR_DELETE_SEGMENT);
        mOutput.println(segmentId);
    }

    public void responseForRequestForDeleteSegment() {
        try {
            Long segmentId = Long.parseLong(mInput.readLine());
            FileManager.deleteSegment(segmentId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
