package com.filesynch.gui;

import com.filesynch.server.Server;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StartServerButtonEventListener implements ActionListener {
    private Server server;
    private JTextField serverPortField;

    public StartServerButtonEventListener(Server server, JTextField serverPortField) {
        super();
        this.server = server;
        this.serverPortField = serverPortField;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            server.start(Integer.parseInt(serverPortField.getText()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
