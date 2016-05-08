package com.smartcloud.web;

import com.smartcloud.database.ServerDatabase;
import com.smartcloud.holder.CloudHolder;
import com.smartcloud.holder.FileHolder;
import com.smartcloud.util.Util;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class HTMLCreator {
    private static String FILE_UPLOAD_HTML = "<form method='post' enctype='multipart/form-data'><input type='file' name='file' /><input type='submit' value='Send' /></form>";

    public static String createResponseHTML() {
        StringBuilder sb = new StringBuilder("<html><head><title>SmartCloud</title></head><body><h1>SmartCloud</h1>");
        sb.append(cloudFilesHTML());
        sb.append(FILE_UPLOAD_HTML);
        sb.append(cloudStatisticsHTML());
        sb.append("</body></html>");
        return sb.toString();
    }

    public static String cloudFilesHTML() {
        StringBuilder sb = new StringBuilder("<h2>Files</h2><ul>");
        for (FileHolder fileHolder : ServerDatabase.instance.selectFile()) {
            sb.append("<li>");
            sb.append("<a href='/fileId=");
            sb.append(fileHolder.getId());
            sb.append("' download='");
            sb.append(fileHolder.getName());
            sb.append("'>");
            sb.append(fileHolder.getName());
            sb.append(" (");
            sb.append(Util.sizeToReadableUnit(fileHolder.getSize()));
            sb.append(") ");
            sb.append("</a>");
            sb.append("<a href='/deleteFileId=");
            sb.append(fileHolder.getId());
            sb.append("' style='color:red'>-");
            sb.append("</a>");
            sb.append("</li>");
        }
        sb.append("</ul><br>");
        return sb.toString();
    }

    public static String cloudStatisticsHTML() {
        StringBuilder sb = new StringBuilder("<h2>Statistics</h2>");
        sb.append("<h3>Machines</h3>").append(tableHTML(new String[]{"Id", "Machine Role"}, CloudHolder.getMachines(), new String[]{"getId", "getMachineRole"}));
        sb.append("<h3>Files</h3>").append(tableHTML(new String[]{"Id", "Name", "Size"}, ServerDatabase.instance.selectFile(), new String[]{"getId", "getName", "getSize"}));
        sb.append("<h3>Segments</h3>").append(tableHTML(new String[]{"Id", "File Id", "Machine Id", "Byte From", "Byte To"},
                ServerDatabase.instance.selectSegment(null), new String[]{"getId", "getFileId", "getMachineId", "getByteFrom", "getByteTo"}));
        return sb.toString();
    }

    private static String tableHTML(String[] labels, List<? extends Object> rows, String[] methodNames) {
        StringBuilder sb = new StringBuilder("<table><thead><tr>");
        for (String label : labels) {
            sb.append("<th>").append(label).append("</th>");
        }
        sb.append("</tr></thead><tbody>");
        for (Object row : rows) {
            sb.append("<tr>");
            for (String methodName : methodNames) {
                try {
                    sb.append("<td>").append(row.getClass().getMethod(methodName).invoke(row).toString()).append("</td>");
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
            sb.append("</tr>");
        }
        sb.append("</tbody></table>");
        return sb.toString();
    }
}
