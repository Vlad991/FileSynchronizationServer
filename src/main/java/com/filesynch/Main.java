package com.filesynch;

import com.filesynch.client_server.Server;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.Scanner;

public class Main {
//    private static MainGUI app;

    public static void main(String[] args) throws IOException {
//        app = new MainGUI();
//        app.setVisible(true);
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {

            LocateRegistry.createRegistry(1099);
            Server fs = new Server();
            Naming.rebind("rmi://localhost/fs", fs);
            System.out.println("File Server is Ready");
            //sendMessages("admin", fs);
            sendFiles("admin", fs);
        } catch (Exception e) {
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

//    public static MainGUI getMainGUI() {
//        return app;
//    }
}
