package com.filesynch.gui;

import com.filesynch.server.Server;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StopServerButtonEventListener implements ActionListener {
    private Server server;

    public StopServerButtonEventListener(Server server) {
        super();
        this.server = server;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            server.stop();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
