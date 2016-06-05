package com.smartcloud.web;

import com.smartcloud.constant.Algorithm;
import com.smartcloud.constant.Redundancy;
import com.smartcloud.database.ServerDatabase;
import com.smartcloud.holder.FileHolder;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class HTMLCreator {

    public static String createResponseHTML() {
        StringBuilder sb = new StringBuilder("<html><head><title>SmartCloud</title></head><body><h1>SmartCloud</h1>");
        sb.append(cloudFilesHTML());
        sb.append(tableAlgorithmHTML());
        sb.append(fileUploadFormHTML());
        sb.append(cloudStatisticsHTML());
        sb.append("</body></html>");
        return sb.toString();
    }

    public static String fileUploadFormHTML() {
        StringBuilder sb = new StringBuilder("<h2>Upload</h2>");
        sb.append("</br>Algorytm: <select id='algorithm' name='algorithm' onchange='selectChange()'>");
        for (Algorithm algorithm : Algorithm.values()) {
            sb.append("<option value='" + algorithm.toString() + "'>" + algorithm.toString() + "</option>");
        }
        sb.append("</select></br>");
        int activeMachines = ServerDatabase.instance.selectMachine(true).size();
        sb.append("Redundancja: <select id='redundancy' name='redundancy' onchange='selectChange()'>");
        if (activeMachines > 1) {
            for (Redundancy redundancy : Redundancy.values()) {
                sb.append("<option value='" + redundancy.toString() + "'>" + redundancy.toString() + "</option>");
            }
        } else {
            sb.append("<option value='" + Redundancy.NONE.toString() + "'>" + Redundancy.NONE.toString() + "</option>");
        }
        sb.append("</select></br>");
        sb.append("Stały rozmiar: <select id='segmentMaxSize' name='segmentMaxSize' onchange='selectChange()'>");
        for (int size : Algorithm.sizes) {
            if (size == 1024) {
                sb.append("<option selected value='" + size + "'>" + size + "KB</option>");
            } else {
                sb.append("<option value='" + size + "'>" + size + "KB</option>");
            }
        }
        sb.append("</select></br>");
        if (activeMachines > 1) {
            sb.append("Wrzucaj segmenty na Master: <input id='uploadToMaster' type='checkbox' onchange='selectChange()'></br>");
        }
        sb.append("<form id='uploadForm' method='post' enctype='multipart/form-data'>");
        sb.append("<input type='file' name='file' /><input type='submit' value='Send' /></form>");
        sb.append("<script>\n" +
                "function selectChange(){\n" +
                "    var algorithm = document.getElementById('algorithm').value;\n" +
                "    var redundancy = document.getElementById('redundancy').value;\n" +
                "    var segmentMaxSize = document.getElementById('segmentMaxSize').value;\n" +
                "    var uploadToMaster = document.getElementById('uploadToMaster') == null ? true : document.getElementById('uploadToMaster').checked;\n" +
                "    document.getElementById('uploadForm').setAttribute('action','/upload?algorithm='+algorithm+'&redundancy='+redundancy+'&segmentMaxSize='+segmentMaxSize+'&uploadToMaster='+uploadToMaster);\n" +
                "}\n" +
                "selectChange();\n" +
                "</script>");
        return sb.toString();
    }

    public static String cloudFilesHTML() {
        StringBuilder sb = new StringBuilder("<h2>Files</h2><ul>");
        for (FileHolder fileHolder : ServerDatabase.instance.selectFile()) {
            if (!fileHolder.isAvailable()) {
                sb.append("<li style='text-decoration:line-through;'>");
                sb.append(fileHolder.getName());
                sb.append(" (");
                sb.append(fileHolder.getSizeReadable());
                sb.append(") ");
                sb.append("<a href='/deleteFileId=");
                sb.append(fileHolder.getId());
                sb.append("' style='color:red;'>-");
                sb.append("</a>");
                sb.append("</li>");
                continue;
            }
            if (fileHolder.isActive()) {
                sb.append("<li>");
                sb.append("<a href='/fileId=");
                sb.append(fileHolder.getId());
                sb.append("' download='");
                sb.append(fileHolder.getName());
                sb.append("'>");
                sb.append(fileHolder.getName());
                sb.append(" (");
                sb.append(fileHolder.getSizeReadable());
                sb.append(") ");
                sb.append("</a>");
                sb.append("<a href='/deleteFileId=");
                sb.append(fileHolder.getId());
                sb.append("' style='color:red'>-");
                sb.append("</a>");
                sb.append("</li>");
            } else {
                sb.append("<li>");
                sb.append(fileHolder.getName());
                sb.append(" (");
                sb.append(fileHolder.getSizeReadable());
                sb.append(") ");
                sb.append("<a href='/deleteFileId=");
                sb.append(fileHolder.getId());
                sb.append("' style='color:red'>-");
                sb.append("</a>");
                sb.append("</li>");
            }
        }
        sb.append("</ul><br>");
        return sb.toString();
    }

    public static String cloudStatisticsHTML() {
        StringBuilder sb = new StringBuilder("<h2>Statistics</h2>");
        List<? extends Object> list = ServerDatabase.instance.selectMachine(true);
        if (!list.isEmpty()) {
            sb.append("<h3>Machines Active</h3>").append(tableDomainHTML(new String[]{"Id", "Machine Role", "Address", "Free Space", "Total Space"}, list,
                    new String[]{"getId", "getMachineRole", "getAddress", "getFreeSpaceReadable", "getTotalSpaceReadable"}));
        }
        list = ServerDatabase.instance.selectMachine(false);
        if (!list.isEmpty()) {
            sb.append("<h3>Machines Inactive</h3>").append(tableDomainHTML(new String[]{"Id", "Machine Role", "Total Space", "Last Contact"}, list,
                    new String[]{"getId", "getMachineRole", "getTotalSpaceReadable", "getLastContactFormat"}));
        }
        list = ServerDatabase.instance.selectFile();
        List<FileHolder> filesActive = new ArrayList<>();
        List<FileHolder> filesInactive = new ArrayList<>();
        List<FileHolder> filesUnavailable = new ArrayList<>();
        for (FileHolder fileHolder : ServerDatabase.instance.selectFile()) {
            if (!fileHolder.isAvailable()) {
                filesUnavailable.add(fileHolder);
                continue;
            }
            if (fileHolder.isActive()) {
                filesActive.add(fileHolder);
            } else {
                filesInactive.add(fileHolder);
            }
        }
        if (!filesActive.isEmpty()) {
            sb.append("<h3>Files Active</h3>").append(tableDomainHTML(new String[]{"Id", "Name", "Size"}, filesActive, new String[]{"getId", "getName", "getSizeReadable"}));
        }
        if (!filesInactive.isEmpty()) {
            sb.append("<h3>Files Inactive</h3>").append(tableDomainHTML(new String[]{"Id", "Name", "Size"}, filesInactive, new String[]{"getId", "getName", "getSizeReadable"}));
        }
        if (!filesUnavailable.isEmpty()) {
            sb.append("<h3>Files Unavailable</h3>").append(tableDomainHTML(new String[]{"Id", "Name", "Size"}, filesUnavailable, new String[]{"getId", "getName", "getSizeReadable"}));
        }
        list = ServerDatabase.instance.selectSegment();
        if (!list.isEmpty()) {
            sb.append("<h3>Segments</h3>").append(tableDomainHTML(new String[]{"Id", "File Id", "Machine Id", "Byte From", "Byte To", "Size"}, list,
                    new String[]{"getId", "getFileId", "getMachineId", "getByteFrom", "getByteTo", "getSizeReadable"}));
        }
        return sb.toString();
    }

    private static String tableDomainHTML(String[] labels, List<? extends Object> rows, String[] methodNames) {
        StringBuilder sb = new StringBuilder("<table><thead><tr>");
        for (String label : labels) {
            sb.append("<th>").append(label).append("</th>");
        }
        sb.append("</tr></thead><tbody>");
        for (Object row : rows) {
            sb.append("<tr>");
            for (String methodName : methodNames) {
                try {
                    Object value = row.getClass().getMethod(methodName).invoke(row);
                    sb.append("<td>").append(value != null ? value.toString() : "").append("</td>");
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

    private static String tableAlgorithmHTML() {
        StringBuilder sb = new StringBuilder("<h2>Algorithms</h2><table><thead><tr>");
        sb.append("<th>").append("Id").append("</th>");
        sb.append("<th>").append("Rozmiar danych").append("</th>");
        sb.append("<th>").append("Dystrybucja").append("</th>");
        sb.append("</tr></thead><tbody>");
        for (Algorithm algorithm : Algorithm.values()) {
            sb.append("<tr>");
            sb.append("<td>").append(algorithm.toString()).append("</td>");
            sb.append("<td>").append(algorithm.segmentDescription);
            sb.append("</td>");
            sb.append("<td>").append(algorithm.distributionDescription).append("</td>");
            sb.append("</tr>");
        }
        sb.append("</tbody></table>");
        return sb.toString();
    }
}
