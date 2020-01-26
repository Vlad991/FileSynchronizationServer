package com.filesynch.gui;

import com.filesynch.server.Server;

import javax.swing.*;
import java.awt.*;

public class ServerManagePanel extends JPanel {
    private JLabel serverLabel = new JLabel("Server: ");
    private JButton startServerButton = new JButton("start");
    private JButton stopServerButton = new JButton("stop");
    private JLabel serverPortLabel = new JLabel("Port:");
    private JTextField serverPortField = new JTextField("8888");
    private JLabel serverStatusLabel = new JLabel("Status:");
    private JLabel serverStatusIndicator = new JLabel("Not Running..");
    private LogoImagePanel logoImagePanel = new LogoImagePanel();

    public ServerManagePanel(Server server) {
        startServerButton
                .addActionListener(
                        new StartServerButtonEventListener(
                                server,
                                serverPortField));
        stopServerButton.addActionListener(new StopServerButtonEventListener(server));
        serverStatusIndicator.setOpaque(true);
        serverStatusIndicator.setBackground(Color.RED);
        startServerButton.addMouseListener(new ServerStatusIndicatorListener(server, serverStatusIndicator));
        stopServerButton.addMouseListener(new ServerStatusIndicatorListener(server, serverStatusIndicator));

        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        this.add(logoImagePanel, c);
        c.gridx = 1;
        c.gridy = 0;
        this.add(serverLabel, c);
        c.gridx = 2;
        c.gridy = 0;
        this.add(startServerButton, c);
        c.gridx = 3;
        c.gridy = 0;
        this.add(stopServerButton, c);
        c.gridx = 0;
        c.gridy = 1;
        this.add(serverPortLabel, c);
        c.gridx = 1;
        c.gridy = 1;
        this.add(serverPortField, c);
        c.gridx = 2;
        c.gridy = 1;
        this.add(serverStatusLabel, c);
        c.gridx = 3;
        c.gridy = 1;
        this.add(serverStatusIndicator, c);
    }
}
