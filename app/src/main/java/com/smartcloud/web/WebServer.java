package com.smartcloud.web;

import com.smartcloud.algorithm.DownloadStreamingAlgorithm;
import com.smartcloud.algorithm.UploadSegmentationAlgorithm;
import com.smartcloud.holder.FileHolder;
import com.smartcloud.task.MasterGetMachineHolderFromSlaveTask;

import java.io.FileNotFoundException;
import java.io.IOException;

public class WebServer extends NanoHTTPD {
    public static final int PORT = 8080;

    public static final int REQUEST_BUFFER_LEN = 512;

    public WebServer() {
        super(PORT);
        setTempFileManagerFactory(new FileManagerFactory());
    }

    public static Response newFixedLengthResponse(Response.IStatus status, String mimeType, String message) {
        Response response = NanoHTTPD.newFixedLengthResponse(status, mimeType, message);
        response.addHeader("Accept-Ranges", "bytes");
        return response;
    }

    public void start() {
        try {
            super.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        super.stop();
    }

    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        String uri = session.getUri();
        if (Method.PUT.equals(method) || Method.POST.equals(method)) {
            try {
                new UploadSegmentationAlgorithm((HTTPSession) session).perform();
//                session.parseBody(null);
//                File file = new File(files.get("file"));
//                FileManager.manageUploadedFile(file);
            } catch (IOException ioe) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
            } catch (ResponseException re) {
                return newFixedLengthResponse(re.getStatus(), NanoHTTPD.MIME_PLAINTEXT, re.getMessage());
            }
        } else if (Method.GET.equals(method) && uri.contains("/fileId=")) {
            Long fileId = Long.parseLong(uri.replace("/fileId=", ""));
            return new DownloadStreamingAlgorithm(fileId, getMimeTypeForFile(uri)).perform();
//
//            FileHolder fileHolder = ServerDatabase.instance.selectFile(fileId);
//            Response response = serveFile(fileHolder, getMimeTypeForFile(uri));
//            FileManager.addDeleteFileListener(fileHolder.getFile(), response);
//            return response;
        } else if (Method.GET.equals(method) && uri.contains("/deleteFileId=")) {
            Long fileId = Long.parseLong(uri.replace("/deleteFileId=", ""));
            FileHolder.deleteFile(fileId);
//            FileManager.manageDeleteFile(fileId);
        }
//        Map<String, String> header = session.getHeaders();
//        Map<String, String> parms = session.getParms();
//        System.out.println(session.getMethod() + " '" + uri + "' ");
//
//        Iterator<String> e = header.keySet().iterator();
//        while (e.hasNext()) {
//            String value = e.next();
//            System.out.println("  HDR: '" + value + "' = '" + header.get(value) + "'");
//        }
//        e = parms.keySet().iterator();
//        while (e.hasNext()) {
//            String value = e.next();
//            System.out.println("  PRM: '" + value + "' = '" + parms.get(value) + "'");
//        }
        MasterGetMachineHolderFromSlaveTask.updateMachines();
        return newFixedLengthResponse(HTMLCreator.createResponseHTML());
    }

    public static Response getForbiddenResponse(String s) {
        return newFixedLengthResponse(Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: " + s);
    }


}