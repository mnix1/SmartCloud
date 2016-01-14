package com.smartcloud.file;

import com.smartcloud.database.ClientDatabase;
import com.smartcloud.holder.MachineHolder;

import java.io.File;
import java.util.UUID;

public class ConfigManager {
    public static File configFile;

    public static void importMe() {
        MachineHolder machineHolder = ClientDatabase.instance.selectMachine();
        String myId = machineHolder != null ? machineHolder.getId() : null;
        if (myId == null) {
            myId = UUID.randomUUID().toString();
            ClientDatabase.instance.insertMachine(myId);
        }
//        if (configFile.exists()) {
//            try {
//                Scanner scanner = new Scanner(new FileReader(configFile));
//                while (scanner.hasNextLine()) {
//                    String line = scanner.nextLine();
//                    if (line.contains("myId=")) {
//                        myId = line.replace("myId=", "");
//                        scanner.close();
//                        MachineHolder.ME.setId(myId);
//                        return;
//                    }
//                }
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//        } else {
//            try {
//                configFile.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

//        try {
//            PrintWriter printWriter = new PrintWriter(new BufferedWriter(new FileWriter(configFile, true)));
//            printWriter.println("myId=" + myId);
//            printWriter.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        MachineHolder.ME.setId(myId);
    }
}
