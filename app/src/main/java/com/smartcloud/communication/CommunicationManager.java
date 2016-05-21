package com.smartcloud.communication;

import android.util.Base64;

import com.smartcloud.holder.SegmentHolder;
import com.smartcloud.util.FileManager;
import com.smartcloud.util.Util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
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

    public void sendSegmentData(byte[] data, SegmentHolder segmentHolder) {
        mOutput.println(segmentHolder.getId());
        mOutput.println(data.length);
        mOutput.println(new String(Base64.encode(data, Base64.NO_WRAP)));
    }

    public SegmentHolder receiveSegmentDataAndSaveAsFile() {
        SegmentHolder segmentHolder = null;
        FileOutputStream fileOutputStream = null;
        try {
            Long segmentId = Long.parseLong(mInput.readLine());
            int fileSize = Integer.parseInt(mInput.readLine());
            segmentHolder = new SegmentHolder(segmentId, FileManager.storageDir + "/" + segmentId);
            System.out.println("ClientDatabase.instance.insertSegment(segmentHolder);");
            File file = new File(segmentHolder.getPath());
            fileOutputStream = new FileOutputStream(file);
            int totalRead = 0;
            while (totalRead < fileSize) {
                byte[] bytes = Base64.decode(mInput.readLine().getBytes(), Base64.NO_WRAP);
                fileOutputStream.write(bytes);
                fileOutputStream.flush();
                totalRead += bytes.length;
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
        System.out.println("receiveSegmentDataAndSaveAsFile end");
        return segmentHolder;
    }

    public void sendSegmentFromFile(SegmentHolder segmentHolder) {
        File file = new File(segmentHolder.getPath());
        int fileSize = (int) file.length();
        mOutput.println(segmentHolder.getId());
        mOutput.println(fileSize);
        try {
            Util.readFromFileWriteToStream(file, mOutput, mSocket.getSendBufferSize());
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
            int size = Integer.parseInt(mInput.readLine());
            int totalRead = 0;
            while (totalRead < size) {
                byte[] bytes = Base64.decode(mInput.readLine().getBytes(), Base64.NO_WRAP);
                outputStream.write(bytes);
                totalRead += bytes.length;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//    public void writeFile(Long segmentId, FileHolder fileHolder) {
//        File file = fileHolder.getFile();
//        int fileSize = (int) file.length();
//        System.out.println("writeFile start fileSize " + fileSize + " path " + file);
//        FileInputStream fileInputStream = null;
//        mOutput.println(fileHolder.getId());
//        mOutput.println(segmentId);
//        mOutput.println(fileSize);
//        try {
//            fileInputStream = new FileInputStream(file);
//            int bufferSize = mSocket.getSendBufferSize();
//            byte[] buffer = new byte[bufferSize];
//            int read;
//            while ((read = fileInputStream.read(buffer)) >= 0) {
//                if (read == bufferSize) {
//                    mOutput.println(new String(Base64.encode(buffer, Base64.NO_WRAP)));
//                } else {
//                    mOutput.println(new String(Base64.encode(Arrays.copyOf(buffer, read), Base64.NO_WRAP)));
//                }
//                System.out.println("writeFile read " + read);
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (SocketException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                fileInputStream.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        System.out.println("writeFile end");
//    }


//
//    public File readFile() {
//        Long fileId = null;
//        Long segmentId = null;
//        File file = null;
//        FileOutputStream fileOutputStream = null;
//        try {
//            fileId = Long.parseLong(mInput.readLine());
//            segmentId = Long.parseLong(mInput.readLine());
//            file = new File(FileManager.storageDir + "/" + fileId + "^_^" + segmentId);
//            int fileSize = Integer.parseInt(mInput.readLine());
//            System.out.println("readFile start fileSize " + fileSize + " path " + file);
//            fileOutputStream = new FileOutputStream(file);
//            int totalRead = 0;
//            while (totalRead < fileSize) {
//                byte[] bytes = Base64.decode(mInput.readLine().getBytes(), Base64.NO_WRAP);
//                fileOutputStream.write(bytes);
//                fileOutputStream.flush();
//                totalRead += bytes.length;
//                System.out.println("readFile read " + bytes.length + " totalRead " + totalRead);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                fileOutputStream.close();
//                CloudHolder.updateFilesToManage(fileId, segmentId, true);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        System.out.println("readFile end");
//        return file;
//    }
//


    public byte[] readSegmentData() {
        Long segmentId = null;
        File file = null;
        byte[] output = null;
        try {
            segmentId = Long.parseLong(mInput.readLine());
            int fileSize = Integer.parseInt(mInput.readLine());
            System.out.println("readSegmentData start fileSize " + fileSize + " path " + file);
            int totalRead = 0;
            output = new byte[fileSize];
            while (totalRead < fileSize) {
                byte[] bytes = Base64.decode(mInput.readLine().getBytes(), Base64.NO_WRAP);
                System.arraycopy(bytes, 0, output, totalRead, bytes.length);
                totalRead += bytes.length;
                System.out.println("readFile read " + bytes.length + " totalRead " + totalRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("readSegmentData end");
        return output;
    }

    //
//
//    public void writeSegment(FileHolder fileHolder, SegmentHolder segmentHolder) {
//        System.out.println("writeSegment start from " + segmentHolder.getByteFrom() + " to " + segmentHolder.getByteTo());
//        FileInputStream fileInputStream = null;
//        mOutput.println(TaskType.SEND_SEGMENT);
//        long totalToRead = segmentHolder.getByteTo() - segmentHolder.getByteFrom() + 1;
//        mOutput.println(segmentHolder.getId());
//        mOutput.println(totalToRead);
//        try {
//            fileInputStream = new FileInputStream(fileHolder.getFile());
//            fileInputStream.skip(segmentHolder.getByteFrom());
//            int bufferSize = mSocket.getSendBufferSize();
//            byte[] buffer = new byte[bufferSize];
//            int totalRead = 0;
//            int read = 0;
//            while ((read = fileInputStream.read(buffer, 0, Math.min((int) totalToRead - totalRead, bufferSize))) > 0 && totalRead < totalToRead) {
//                if (read == bufferSize) {
//                    mOutput.println(new String(Base64.encode(buffer, Base64.NO_WRAP)));
//                } else {
//                    mOutput.println(new String(Base64.encode(Arrays.copyOf(buffer, read), Base64.NO_WRAP)));
//                }
//                System.out.println("writeSegment read " + read);
//                totalRead += read;
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (SocketException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                fileInputStream.close();
//                segmentHolder.setReady(true);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        System.out.println("writeSegment end");
//    }

    //
//
//
//    public void requestForSendSegment(Long fileId, Long segmentId) {
//        mOutput.println(TaskType.REQUEST_FOR_SEND_SEGMENT_DATA);
//        mOutput.println(fileId);
//        mOutput.println(segmentId);
//    }
//
//    public void responseForRequestForSendSegment() {
//        try {
//            Long fileId = Long.parseLong(mInput.readLine());
//            Long segmentId = Long.parseLong(mInput.readLine());
//            SegmentHolder segmentHolder = ClientDatabase.instance.selectSegment(segmentId);
//            File file = new File(segmentHolder.getPath());
//            FileHolder fileHolder = new FileHolder(fileId, file);
//            mOutput.println(TaskType.RESPONSE_FOR_SEND_SEGMENT_DATA);
//            writeFile(segmentId, fileHolder);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void receiveSegment() {
//        readFile();
//    }
//
//    public void requestForDeleteSegment(Long segmentId) {
//        mOutput.println(TaskType.REQUEST_FOR_DELETE_SEGMENT);
//        mOutput.println(segmentId);
//    }
//
//    public void responseForRequestForDeleteSegment() {
//        try {
//            Long segmentId = Long.parseLong(mInput.readLine());
//            FileManager.deleteSegment(segmentId);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


    public PrintWriter getOutput() {
        return mOutput;
    }

    public void setOutput(PrintWriter output) {
        this.mOutput = output;
    }

    public BufferedReader getInput() {
        return mInput;
    }

    public void setInput(BufferedReader input) {
        this.mInput = input;
    }

    public Socket getSocket() {
        return mSocket;
    }
}
