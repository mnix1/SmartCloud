package com.smartcloud.web;

import com.smartcloud.algorithm.DownloadStreamingAlgorithm;
import com.smartcloud.algorithm.UploadStreamingAlgorithm;
import com.smartcloud.holder.FileHolder;
import com.smartcloud.task.MasterGetMachineHolderFromSlaveTask;

import java.io.IOException;

public class WebServer extends NanoHTTPD {
    public static final int PORT = 8080;

    public static final int REQUEST_BUFFER_LEN = 512;

    public WebServer() {
        super(PORT);
        setTempFileManagerFactory(new FileManagerFactory());
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
        if (Method.GET.equals(method) && uri.equals("/")) {
            MasterGetMachineHolderFromSlaveTask.updateMachines();
            return newFixedLengthResponse(HTMLCreator.createResponseHTML());
        }
        if (Method.PUT.equals(method) || Method.POST.equals(method)) {
            try {
                new UploadStreamingAlgorithm((HTTPSession) session).perform();
                return redirect();
            } catch (IOException ioe) {
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
            } catch (ResponseException re) {
                return newFixedLengthResponse(re.getStatus(), NanoHTTPD.MIME_PLAINTEXT, re.getMessage());
            }
        } else if (Method.GET.equals(method) && uri.contains("/fileId=")) {
            Long fileId = Long.parseLong(uri.replace("/fileId=", ""));
            return new DownloadStreamingAlgorithm(fileId, getMimeTypeForFile(uri)).perform();
        } else if (Method.GET.equals(method) && uri.contains("/deleteFileId=")) {
            Long fileId = Long.parseLong(uri.replace("/deleteFileId=", ""));
            FileHolder.deleteFile(fileId);
            return redirect();
        }
        return newFixedLengthResponse("Smart Cloud");
    }

    public static Response redirect() {
        Response res = newFixedLengthResponse(Response.Status.REDIRECT, NanoHTTPD.MIME_HTML, HTMLCreator.createResponseHTML());
        res.addHeader("Location", "/");
        return res;
    }

    public static Response newFixedLengthResponse(Response.IStatus status, String mimeType, String message) {
        Response response = NanoHTTPD.newFixedLengthResponse(status, mimeType, message);
        response.addHeader("Accept-Ranges", "bytes");
        return response;
    }

    public static Response getForbiddenResponse(String s) {
        return newFixedLengthResponse(Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: " + s);
    }
}