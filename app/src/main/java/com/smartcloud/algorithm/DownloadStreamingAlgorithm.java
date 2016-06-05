package com.smartcloud.algorithm;

import com.smartcloud.communication.ClientCommunication;
import com.smartcloud.connection.ConnectionServer;
import com.smartcloud.database.ClientDatabase;
import com.smartcloud.database.ServerDatabase;
import com.smartcloud.exception.FileNotAvailable;
import com.smartcloud.holder.MachineHolder;
import com.smartcloud.holder.SegmentHolder;
import com.smartcloud.task.MasterGetSegmentDataFromSlaveAndWriteToStreamTask;
import com.smartcloud.task.Task;
import com.smartcloud.util.Util;
import com.smartcloud.web.NanoHTTPD;
import com.smartcloud.web.WebServer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class DownloadStreamingAlgorithm extends DownloadAlgorithm {
    private int mBufferSize = 16 * 1024;

    public DownloadStreamingAlgorithm(Long fileId, String mime) {
        super(ServerDatabase.instance.selectFile(fileId), mime);
    }

    public NanoHTTPD.Response perform() {
        if (mFileHolder == null) {
            return WebServer.getForbiddenResponse("FILE DOES NOT EXISTS");
        }
        NanoHTTPD.Response res = new StreamingResponse(NanoHTTPD.Response.Status.OK, mMime);
        res.addHeader("Accept-Ranges", "bytes");
        res.addHeader("Content-Length", "" + mFileHolder.getSize());
        return res;
    }

    public class StreamingResponse extends NanoHTTPD.Response {
        public StreamingResponse(IStatus status, String mimeType) {
            super(status, mimeType, null, 0);
            setContentLength(mFileHolder.getSize());
        }


        @Override
        protected void sendBody(OutputStream outputStream, long pending) throws IOException {
            long byteTo = -1;
            for (SegmentHolder segmentHolder : ServerDatabase.instance.selectSegment(mFileHolder.getId())) {
                if (!segmentHolder.isActive() || segmentHolder.getByteFrom() - 1 < byteTo) {
                    continue;
                }
                byteTo = segmentHolder.getByteTo();
                MachineHolder machineHolder = ServerDatabase.instance.selectMachine(segmentHolder.getMachineId());
                if (machineHolder.isServer()) {
                    SegmentHolder localSegmentHolder = ClientDatabase.instance.selectSegment(segmentHolder.getId());
                    File file = new File(localSegmentHolder.getPath());
                    Util.readWrite(file, outputStream, mBufferSize);
                } else {
                    Task task = new MasterGetSegmentDataFromSlaveAndWriteToStreamTask(segmentHolder, outputStream);
                    new ClientCommunication(new Socket(InetAddress.getByName(machineHolder.getAddress()),
                            ConnectionServer.SERVER_PORT), task).init();
                }
            }
            if (byteTo + 1 < mFileHolder.getSize()) {
                throw new FileNotAvailable();
            }
        }
    }
}
