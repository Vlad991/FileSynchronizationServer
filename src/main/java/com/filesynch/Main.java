package com.filesynch;

import com.filesynch.gui.MainGUI;
import com.filesynch.server.Server;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class Main {
    private static MainGUI app;

    public static void main(String[] args) throws IOException {
//        app = new MainGUI();
//        app.setVisible(true);
        try {

            LocateRegistry.createRegistry(1099);

            Server fs = new Server();
            fs.setFile("src/main/resources/out/video.mp4");
            Naming.rebind("rmi://localhost/fs", fs);
            System.out.println("File Server is Ready");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public static MainGUI getMainGUI() {
//        return app;
//    }
}
