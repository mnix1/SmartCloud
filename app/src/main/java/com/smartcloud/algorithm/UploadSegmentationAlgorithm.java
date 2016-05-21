package com.smartcloud.algorithm;

import com.smartcloud.communication.ClientCommunication;
import com.smartcloud.connection.ConnectionServer;
import com.smartcloud.database.ClientDatabase;
import com.smartcloud.database.ServerDatabase;
import com.smartcloud.holder.FileHolder;
import com.smartcloud.holder.MachineHolder;
import com.smartcloud.holder.SegmentHolder;
import com.smartcloud.task.MasterSendSegmentDataToSlaveTask;
import com.smartcloud.task.Task;
import com.smartcloud.util.FileManager;
import com.smartcloud.web.NanoHTTPD;
import com.smartcloud.web.WebServer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class UploadSegmentationAlgorithm extends UploadAlgorithm {
    private int mSegmentSize = 1024000;

    public UploadSegmentationAlgorithm(NanoHTTPD.HTTPSession session) {
        super(session);
    }

    public void perform() throws IOException, NanoHTTPD.ResponseException {
        NanoHTTPD.ContentType contentType = new NanoHTTPD.ContentType(mSession.headers.get("content-type"));
        if (!NanoHTTPD.Method.POST.equals(mSession.method) || !contentType.isMultipart()) {
            return;
        }
        byte[] segment = new byte[mSegmentSize];
        int totalOffset = 0;
        int startOffset = 0;
        int totalSize = 0;
        FileHolder fileHolder = null;
        long size = mSession.getBodySize();
        fileHolder = new FileHolder(size);
        byte[] buf = new byte[WebServer.REQUEST_BUFFER_LEN];
        mSession.rlen = mSession.inputStream.read(buf, 0, (int) Math.min(size, WebServer.REQUEST_BUFFER_LEN));
        size -= mSession.rlen;
        if (mSession.rlen > 0) {
            startOffset = decodeMultipartFormDataStart(contentType, buf, mSession.parms);
            int length = decodeMultipartFormDataEnd(contentType, buf, mSession.parms.get("boundaryId"));
            if (length >= 0) {
                totalSize += length - startOffset;
            } else {
                totalSize += mSession.rlen - startOffset;
            }
            fileHolder.setName(mSession.parms.get("file"));
            ServerDatabase.instance.insertFile(fileHolder);
            totalOffset = segmentation(segment, totalOffset, buf, startOffset, totalSize, fileHolder);
        }
        if (size > WebServer.REQUEST_BUFFER_LEN * 2) {
            while (mSession.rlen >= 0 && size > WebServer.REQUEST_BUFFER_LEN * 2) {
                mSession.rlen = mSession.inputStream.read(buf, 0, WebServer.REQUEST_BUFFER_LEN);
                size -= mSession.rlen;
                if (mSession.rlen > 0) {
                    totalOffset = segmentation(segment, totalOffset, buf, 0, mSession.rlen, fileHolder);
                    totalSize += mSession.rlen;
                }
            }
        }
        if (size > WebServer.REQUEST_BUFFER_LEN) {
            mSession.rlen = mSession.inputStream.read(buf, 0, (int) size - WebServer.REQUEST_BUFFER_LEN);
            size -= mSession.rlen;
            if (mSession.rlen > 0) {
                totalOffset = segmentation(segment, totalOffset, buf, 0, mSession.rlen, fileHolder);
                totalSize += mSession.rlen;
            }
        }
        if (size > 0) {
            mSession.rlen = mSession.inputStream.read(buf, 0, (int) WebServer.REQUEST_BUFFER_LEN);
            size -= mSession.rlen;
            if (mSession.rlen > 0) {
                int length = decodeMultipartFormDataEnd(contentType, buf, mSession.parms.get("boundaryId"));
                totalOffset = segmentation(segment, totalOffset, buf, 0, length, fileHolder);
                totalSize += length;
            }
        }
        if (totalOffset % mSegmentSize > 0) {
            SegmentHolder segmentHolder = new SegmentHolder(null, fileHolder.getId(), null, (long) totalSize - (totalOffset % mSegmentSize), (long) (totalSize));
            manageUploadedFile(Arrays.copyOf(segment, totalOffset % mSegmentSize), fileHolder, segmentHolder);
        }
        fileHolder.setSize((long) totalSize);
        ServerDatabase.instance.updateFile(fileHolder);
    }


    private int segmentation(byte[] segment, int totalOffset, byte[] buf, int offset, int length, FileHolder fileHolder) throws
            IOException {
        int segmentOffset = totalOffset % segment.length;
        int size = segment.length - segmentOffset;
        while (length > 0) {
            int len = Math.min(length, size);
            System.arraycopy(buf, offset, segment, segmentOffset, len);
            length -= len;
            offset += len;
            segmentOffset = (segmentOffset + len) % segment.length;
            size -= len;
            totalOffset += len;
            if (size == 0) {
                SegmentHolder segmentHolder = new SegmentHolder(null, fileHolder.getId(), null, (long) totalOffset - segment.length, (long) (totalOffset));
                manageUploadedFile(Arrays.copyOf(segment, segment.length), fileHolder, segmentHolder);
                size = segment.length;
            }
        }
        return totalOffset;
    }

    private int decodeMultipartFormDataStart(NanoHTTPD.ContentType contentType, byte[] buf, Map<String, String> parms) throws NanoHTTPD.ResponseException, IOException {
        int pcount = 0;
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader in =
                new BufferedReader(new InputStreamReader(new ByteArrayInputStream(buf), Charset.forName(contentType.getEncoding())));
        String mpline = in.readLine();
        int offset = 0;
        parms.put("boundaryId", mpline);
        stringBuilder.append(mpline);
        offset += 2;
        if (mpline == null || !mpline.contains(contentType.getBoundary())) {
            throw new NanoHTTPD.ResponseException(NanoHTTPD.Response.Status.BAD_REQUEST, "BAD REQUEST: Content type is multipart/form-data but chunk does not start with boundary.");
        }

        String partName = null, fileName = null;
        // Parse the reset of the header lines
        mpline = in.readLine();
        stringBuilder.append(mpline);
        offset += 2;
        while (mpline != null && mpline.trim().length() > 0) {
            Matcher matcher = NanoHTTPD.CONTENT_DISPOSITION_PATTERN.matcher(mpline);
            if (matcher.matches()) {
                String attributeString = matcher.group(2);
                matcher = NanoHTTPD.CONTENT_DISPOSITION_ATTRIBUTE_PATTERN.matcher(attributeString);
                while (matcher.find()) {
                    String key = matcher.group(1);
                    if ("name".equalsIgnoreCase(key)) {
                        partName = matcher.group(2);
                    } else if ("filename".equalsIgnoreCase(key)) {
                        fileName = matcher.group(2);
                        // add these two line to support multiple
                        // files uploaded using the same field Id
                        if (!fileName.isEmpty()) {
                            if (pcount > 0)
                                partName = partName + String.valueOf(pcount++);
                            else
                                pcount++;
                        }
                    }
                }
            }
            mpline = in.readLine();
            stringBuilder.append(mpline);
            offset += 2;
        }
        if (fileName != null) {
            parms.put(partName, fileName);
        }
        return stringBuilder.length() + offset;
    }

    private int decodeMultipartFormDataEnd(NanoHTTPD.ContentType contentType, byte[] buf, String boundaryId) throws NanoHTTPD.ResponseException {
        try {
            String data = new String(buf);
            return data.lastIndexOf(boundaryId) - 2;
        } catch (Exception e) {
            throw new NanoHTTPD.ResponseException(NanoHTTPD.Response.Status.INTERNAL_ERROR, e.toString());
        }
    }

    private static SecureRandom random = new SecureRandom();

    public static void manageUploadedFile(byte[] data, FileHolder fileHolder, SegmentHolder segmentHolder) {
        List<MachineHolder> machines = ServerDatabase.instance.selectMachine(true);
        MachineHolder machineHolder = machines.get(random.nextInt(machines.size()));
        segmentHolder.setMachineId(machineHolder.getId());
        ServerDatabase.instance.insertSegment(segmentHolder);
        if (machineHolder.isServer()) {
            FileOutputStream fileOutputStream = null;
            segmentHolder.setPath(FileManager.storageDir + "/" + fileHolder.getId() + "^_^" + segmentHolder.getId());
            ClientDatabase.instance.insertSegment(segmentHolder);
            try {
                File file = new File(segmentHolder.getPath());
                file.createNewFile();
                fileOutputStream = new FileOutputStream(file, false);
                fileOutputStream.write(data);
                segmentHolder.setReady(true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                Task task = new MasterSendSegmentDataToSlaveTask(data, segmentHolder);
                new ClientCommunication(new Socket(InetAddress.getByName(machineHolder.getAddress()), ConnectionServer.SERVER_PORT), task).init();
            } catch (IOException e) {
                e.printStackTrace();
            }

//            machineHolder.getCommunicationManager().sendSegmentData(data, segmentHolder);
//            List<CommunicationTaskHolder> tasks = machineHolder.getCommunicationManager().tasks;
//            synchronized (tasks) {
//                System.out.println("TASK ADDED " + TaskType.SEND_SEGMENT_DATA);
//                tasks.add(new CommunicationTaskHolder(TaskType.SEND_SEGMENT_DATA, data, segmentHolder));
//                System.out.println("TASK ADDED END");
//            }
        }
    }

    public int getSegmentSize() {
        return mSegmentSize;
    }

    public void setSegmentSize(int mSegmentSize) {
        this.mSegmentSize = mSegmentSize;
    }
}
