package com.smartcloud.communication;

import android.util.Base64;

import com.smartcloud.holder.SegmentHolder;
import com.smartcloud.util.FileManager;
import com.smartcloud.util.Util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.NoSuchElementException;

public abstract class CommunicationManager {
    protected Socket mSocket;
    protected BufferedReader mInput;
    protected PrintWriter mOutput;
    protected Thread mThread;

    protected CommunicationManager(Socket socket) {
        this.mSocket = socket;
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
//            mOutput.println(message.length());
            mOutput.println(message);
//            mOutput.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Object receiveObject() {
        try {
//            int messageLength = Integer.parseInt(mInput.readLine());
//            char[] chars = new char[messageLength];
//            int read = 0;
//            while (read < messageLength) {
//                read += mInput.read(chars, read, messageLength);
//            }
           String message =  mInput.readLine();
            return Util.deserializeFromString(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public SegmentHolder receiveSegmentDataAndSaveAsFile(Long segmentId) {
        SegmentHolder segmentHolder = null;
        try {
            Long inputSegmentId = Long.parseLong(mInput.readLine());
            if (segmentId == null) {
                segmentId = inputSegmentId;
            }
            segmentHolder = new SegmentHolder(segmentId, FileManager.storageDir + "/" + segmentId);
            File file = new File(segmentHolder.getPath());
            int size = Integer.parseInt(mInput.readLine());
            Util.readWrite(mInput, file, size);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return segmentHolder;
    }

    public void sendSegmentFromFile(SegmentHolder segmentHolder) {
        File file = new File(segmentHolder.getPath());
        int fileSize = (int) file.length();
        mOutput.println(segmentHolder.getId());
        mOutput.println(fileSize);
        try {
            Util.readWrite(file, mOutput, mSocket.getSendBufferSize());
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void receiveSegmentDataAndWriteToStream(Long segmentId, OutputStream outputStream) {
        try {
            Long receivedSegmentId = Long.parseLong(mInput.readLine());
            if (segmentId != null && !receivedSegmentId.equals(segmentId)) {
                throw new NoSuchElementException("DIFFERENT ID");
            }
            Util.readWrite(mInput, outputStream, Integer.parseInt(mInput.readLine()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PrintWriter getOutput() {
        return mOutput;
    }

    public Socket getSocket() {
        return mSocket;
    }
}
