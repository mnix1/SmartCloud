package com.smartcloud.util;

import android.os.Environment;

import java.io.File;

public class FileManager {
    public static File storageDir;
    public static File rootDir;

    public static void setDirs() {
        String rootDirPath = Environment.getExternalStorageDirectory() + "/SmartCloud";
        rootDir = new File(rootDirPath);
        storageDir = new File(rootDirPath + "/storage");
        if (!rootDir.exists()) {
            rootDir.mkdirs();
        }
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
    }

    public static Long getFreeSpace() {
        return rootDir.getFreeSpace();
    }

    public static Long getTotalSpace() {
        return rootDir.getTotalSpace();
    }

//    public static void manageUploadedFile(File file) {
//        FileHolder fileHolder = new FileHolder(file);
//        ServerDatabase.instance.insertFile(fileHolder);
//        List<MachineHolder> machines = ServerDatabase.instance.selectMachine();
//        sendSegments(fileHolder, machines);
//    }

//    private static void sendSegments(final FileHolder fileHolder, List<MachineHolder> machines) {
//        final List<SegmentHolder> segments = new ArrayList<SegmentHolder>();
//        long from = 0;
//        long to = machines.isEmpty() ? fileHolder.getSize() - 1 : fileHolder.getSize() / machines.size();
//        for (int i = 0; i < machines.size(); i++) {
//            MachineHolder machineHolder = machines.get(i);
//            SegmentHolder segmentHolder = new SegmentHolder(null, fileHolder.getId(), machineHolder.getId(), from, to);
//            segments.add(segmentHolder);
//            ServerDatabase.instance.insertSegment(segmentHolder);
//            System.out.println("sendSegments " + segmentHolder);
//            if (machineHolder.isServer()) {
//                segmentHolder.setPath(FileManager.storageDir + "/" + fileHolder.getId() + "^_^" + segmentHolder.getId());
//                ClientDatabase.instance.insertSegment(segmentHolder);
//                try {
//                    File file = new File(segmentHolder.getPath());
//                    file.createNewFile();
//                    FileOutputStream fileOutputStream = new FileOutputStream(file, false);
//                    FileInputStream fileInputStream = new FileInputStream(fileHolder.getFile());
//                    fileInputStream.skip(from);
//                    int bufferSize = 1024 * 1024;
//                    byte[] buffer = new byte[bufferSize];
//                    long totalToRead = segmentHolder.getByteTo() - segmentHolder.getByteFrom() + 1;
//                    int totalRead = 0;
//                    int read = 0;
//                    while ((read = fileInputStream.read(buffer, 0, Math.min((int) totalToRead - totalRead, bufferSize))) > 0 && totalRead < totalToRead) {
//                        fileOutputStream.write(buffer, 0, read);
//                        totalRead += read;
//                    }
//                    segmentHolder.setReady(true);
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            } else {
//                List<CommunicationTaskHolder> tasks = machineHolder.getCommunicationManager().tasks;
//                synchronized (tasks) {
//                    System.out.println("TASK ADDED " + TaskType.SEND_SEGMENT);
//                    tasks.add(new CommunicationTaskHolder(TaskType.SEND_SEGMENT, fileHolder, segmentHolder));
//                    System.out.println("TASK ADDED END");
//                }
//            }
//            from = to + 1;
//            if (i == machines.size() - 2) {
//                to = fileHolder.getSize() - 1;
//            } else {
//                to += fileHolder.getSize() / machines.size();
//            }
//        }
//        addDeleteFileListener(fileHolder, segments);
//    }

//    public static void manageDownloadFile(FileHolder fileHolder, OutputStream outputStream) {
//        Long fileId = fileHolder.getId();
//        long BUFFER_SIZE = 16 * 1024;
//        byte[] buff = new byte[(int) BUFFER_SIZE];
//        List<SegmentHolder> segments = ServerDatabase.instance.selectSegment(fileId);
////        CloudHolder.addFilesToManage(fileId, segments);
//        for (SegmentHolder segmentHolder : segments) {
//            MachineHolder machineHolder = ServerDatabase.instance.selectMachine(segmentHolder.getMachineId());
//            System.out.println("manageDownloadFile segment " + segmentHolder + " machine " + machineHolder);
//            if (machineHolder.isServer()) {
//                try {
//                    SegmentHolder segment = ClientDatabase.instance.selectSegment(segmentHolder.getId());
//                    FileInputStream fis = new FileInputStream(new File(segment.getPath()));
//                    long pending = segmentHolder.getByteTo() - segmentHolder.getByteFrom();
//                    while (pending > 0) {
//                        long bytesToRead = Math.min(pending, BUFFER_SIZE);
//                        int read = fis.read(buff, 0, (int) bytesToRead);
//                        if (read <= 0) {
//                            break;
//                        }
//                        outputStream.write(buff, 0, read);
//                        pending -= read;
//                    }
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            } else {
////                    List<CommunicationTaskHolder> tasks = machineHolder.getCommunicationManager().tasks;
////                    synchronized (tasks) {
////                        System.out.println("TASK ADDED " + TaskType.REQUEST_FOR_SEND_SEGMENT_DATA);
////                        tasks.add(new CommunicationTaskHolder(TaskType.REQUEST_FOR_SEND_SEGMENT_DATA, fileHolder.getId(), segmentHolder.getId()));
////                        System.out.println("TASK ADDED END");
////                    }
//                CommunicationManager communicationManager = null;
//                try {
//                    communicationManager = new ClientCommunication(new Socket(machineHolder.getAddress(), ConnectionServer.SERVER_PORT));
//                    Thread mClientThread = new Thread(communicationManager);
//                    mClientThread.start();
//                } catch (UnknownHostException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                communicationManager.requestForSendSegment(fileId, segmentHolder.getId());
//                byte[] data = communicationManager.readSegmentData();
//                ByteArrayInputStream bis = new ByteArrayInputStream(data);
//                try {
//                    SegmentHolder segment = ClientDatabase.instance.selectSegment(segmentHolder.getId());
//                    long pending = segment.getByteTo() - segment.getByteFrom();
//                    while (pending > 0) {
//                        long bytesToRead = Math.min(pending, BUFFER_SIZE);
//                        int read = bis.read(buff, 0, (int) bytesToRead);
//                        if (read <= 0) {
//                            break;
//                        }
//                        outputStream.write(buff, 0, read);
//                        pending -= read;
//                    }
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        while (!CloudHolder.isFileReady(fileId)) {
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        File file = fileHolder.getFile();
//        if (file.exists()) {
//            file.delete();
//        }
//        FileOutputStream fileOutputStream = null;
//        try {
//            file.createNewFile();
//            fileOutputStream = new FileOutputStream(file, false);
//            for (SegmentHolder segmentHolder : CloudHolder.getSegmentsOfFileToManage(fileId)) {
//                File segmentFile = new File(storageDir + "/" + fileId + "^_^" + segmentHolder.getId());
//                byte[] buffer = new byte[1024 * 1024];
//                FileInputStream segmentFileInputStream = new FileInputStream(segmentFile);
//                int read;
//                while ((read = segmentFileInputStream.read(buffer)) >= 0) {
//                    fileOutputStream.write(buffer, 0, read);
//                }
//                segmentFileInputStream.close();
//                if (!segmentHolder.getMachineId().equals(MachineHolder.ME.getId())) {
//                    segmentFile.delete();
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                fileOutputStream.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    public static void manageDeleteFile(Long fileId) {
//        List<SegmentHolder> segments = ServerDatabase.instance.selectSegment(fileId);
//        for (SegmentHolder segmentHolder : segments) {
//            MachineHolder machineHolder = ServerDatabase.instance.selectMachine(segmentHolder.getMachineId());
//            if (machineHolder == null) {
//                continue;
//            }
//            System.out.println("manageDeleteFile segment " + segmentHolder + " machine " + machineHolder);
//            if (machineHolder.isServer()) {
//                new File(ClientDatabase.instance.selectSegment(segmentHolder.getId()).getPath()).delete();
//            } else {
//                List<CommunicationTaskHolder> tasks = machineHolder.getCommunicationManager().tasks;
//                synchronized (tasks) {
//                    System.out.println("TASK ADDED " + TaskType.REQUEST_FOR_DELETE_SEGMENT);
//                    tasks.add(new CommunicationTaskHolder(TaskType.REQUEST_FOR_DELETE_SEGMENT, segmentHolder.getId()));
//                    System.out.println("TASK ADDED END");
//                }
//            }
//        }
//        ServerDatabase.instance.deleteFile(fileId);
//        ServerDatabase.instance.deleteSegments(fileId);
//    }

//    public static void deleteSegment(Long segmentId) {
//        SegmentHolder segmentHolder = ClientDatabase.instance.selectSegment(segmentId);
//        File file = new File(segmentHolder.getPath());
//        file.delete();
//        ClientDatabase.instance.deleteSegment(segmentHolder);
//    }
//
//
//    public static void addDeleteFileListener(final File file, final NanoHTTPD.Response response) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (!response.isCloseConnection()) {
////                    System.out.println("WAITING FOR DELETE FILE");
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//                file.delete();
//            }
//        }).start();
//    }
//
//    public static void addDeleteFileListener(final FileHolder fileHolder, final List<SegmentHolder> segments) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                boolean ready = false;
//                while (!ready) {
//                    ready = true;
//                    for (SegmentHolder segmentHolder : segments) {
//                        if (!segmentHolder.isReady()) {
//                            System.out.println("DELETE FILE NOT " + segmentHolder.toString());
//                            ready = false;
//                            try {
//                                Thread.sleep(1000);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                            break;
//                        }
//                    }
//                }
//                System.out.println("DELETE FILE " + fileHolder.getName());
//                fileHolder.getFile().delete();
//            }
//        }).start();
//    }
}
