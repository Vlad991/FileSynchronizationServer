package com.filesynch;

import com.filesynch.client_server.ClientInt;
import com.filesynch.client_server.Logger;
import com.filesynch.client_server.Server;
import com.filesynch.dto.ServerStatus;
import com.filesynch.entity.FileInfoReceived;
import com.filesynch.entity.FileInfoSent;
import com.filesynch.entity.FilePartReceived;
import com.filesynch.entity.FilePartSent;
import com.filesynch.gui.FileSynchronizationServer;
import com.filesynch.gui.NewClient;
import org.springframework.data.domain.Sort;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static JFrame serverFrame;
    public static FileSynchronizationServer fileSynchronizationServer;
    public static Server server;
    public static Registry registry;

    public static void main(String[] args) throws IOException {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        serverFrame = new JFrame("File Synchronization Server");
        fileSynchronizationServer = new FileSynchronizationServer();
        serverFrame.setContentPane(fileSynchronizationServer.getJPanelServer());
        serverFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        serverFrame.pack();
        serverFrame.setLocationRelativeTo(null);
        serverFrame.setVisible(true);
    }

    public static void startServer(int port, String address) {
        if (server == null || server.getServerStatus() == ServerStatus.SERVER_STANDBY_FULL) {
            try {
                registry = LocateRegistry.createRegistry(port);
                server = new Server();
                Naming.rebind("rmi://localhost:" + port + "/" + address, server);
                Logger logger = new Logger();
                logger.log = fileSynchronizationServer.getJTextAreaLog();
                server.setLogger(logger);
                server.setFileProgressBar(fileSynchronizationServer.getJProgressBarFile());
                server.getLogger().log("File Server Started");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        updateClientList();
        updateFileQueue();
    }

    public static void stopServer(int port, String address) {
        if (server.getServerStatus() == ServerStatus.SERVER_WORK || server.getServerStatus() == ServerStatus.SERVER_STANDBY_TRANSFER) {
            try {
                UnicastRemoteObject.unexportObject(registry, true);
                server.getLogger().log("File Server Stopped");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static JFrame showNewClientIcon(ClientInt clientInt, NewClient newClient) {
        JFrame newClientFrame = new JFrame("New Client");
        newClient.setClientInt(clientInt);
        newClientFrame.setContentPane(newClient.getJPanelMain());
        newClientFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        newClientFrame.pack();
        newClientFrame.setLocationRelativeTo(null);
        newClientFrame.setVisible(true);
        return newClientFrame;
    }

    public static void hideNewClientIcon(JFrame jFrame) {
        jFrame.setVisible(false);
    }
    
    public static void updateClientList() {
        HashMap<String, ClientInt> clientIntHashMap = server.getClientIntHashMap();
        DefaultListModel demoList = new DefaultListModel();
        clientIntHashMap.forEach((login, clientInt) -> {
            demoList.addElement(login);
        });
        fileSynchronizationServer.getJListClientList().setModel(demoList);
    }
    
    public static void updateFileQueue() {
        List<FileInfoReceived> fileInfoReceivedList = server.getFileInfoReceivedRepository().findAll();
        List<FileInfoSent> fileInfoSentList = server.getFileInfoSentRepository().findAll();
        //List<FilePartReceived> filePartReceivedList = server.getFilePartReceivedRepository().findAll();
        //List<FilePartSent> filePartSentList = server.getFilePartSentRepository().findAll();

        
        DefaultListModel demoReceivedList = new DefaultListModel();
        DefaultListModel demoSentList = new DefaultListModel();
        for (FileInfoReceived f : fileInfoReceivedList) {
            demoReceivedList.addElement("" + f.getId() + ". " + f.getName() + " receiving from " + f.getClient().getLogin());
        }
        for (FileInfoSent f : fileInfoSentList) {

            demoSentList.addElement("" + f.getId() + ". " + f.getName() + " sending to " + f.getClient().getLogin());
        }

        fileSynchronizationServer.getJListQueueReceiving().setModel(demoReceivedList);
        fileSynchronizationServer.getJListQueueSending().setModel(demoSentList);
    }

    public static void sendMessage(String login, String message) {
        server.sendTextMessageToClient(login, message);
    }

    public static void sendFile(String login, String file) {
        server.sendFileToClient(login, file);
    }

    public static void sendAllFiles(String login) {
        try (Stream<Path> walk = Files.walk(Paths.get(server.FILE_OUTPUT_DIRECTORY
                .substring(0, server.FILE_OUTPUT_DIRECTORY.length() - 1)))) {
            List<String> result = walk.filter(Files::isRegularFile)
                    .map(x -> x.toString()).collect(Collectors.toList());
            for (String filePath : result) {
                sendFile(login, filePath.replace(server.FILE_OUTPUT_DIRECTORY, ""));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendMessages(String login, Server server) {
        Scanner s = new Scanner(System.in);
        boolean run = true;
        while (run) {
            String line = s.nextLine();
            if (line.equals("stop")) {
                run = false;
                break;
            }
            server.sendTextMessageToClient(login, line);
        }
    }

    public static void sendFiles(String login, Server server) {
        Scanner scanner = new Scanner(System.in);
        boolean run = true;
        while (run) {
            String line = scanner.nextLine();
            if (line.equals("stop")) {
                run = false;
                break;
            }
            server.sendFileToClient(login, line);
        }
    }
}
