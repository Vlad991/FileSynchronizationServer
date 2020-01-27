package com.filesynch;

import com.filesynch.client_server.Server;
import com.filesynch.configuration.DataConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public static MainGUI getMainGUI() {
//        return app;
//    }
}
