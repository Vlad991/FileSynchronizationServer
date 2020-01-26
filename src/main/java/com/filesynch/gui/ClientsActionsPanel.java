package com.filesynch.gui;

import javax.swing.*;
import java.awt.*;

public class ClientsActionsPanel extends JPanel {
    private JLabel clientNameLabel = new JLabel("Name:");
    private JTextField clientNameField = new JTextField("", 10);
    private JLabel clientIPLabel = new JLabel("IP-address:");
    private JTextField clientIPAddressField = new JTextField("", 10);
    private JButton addClientButton = new JButton("Add client");
    private TableBuilder tableBuilder;

    public ClientsActionsPanel(TableBuilder tableBuilder) {
        this.tableBuilder = tableBuilder;
        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;

        addClientButton.addMouseListener(
                new AddClientActionListener(clientNameField, clientIPAddressField, tableBuilder)); //на кнопкудобавления клиентов слушатель

        c.gridx = 0;
        c.gridy = 0;
        this.add(clientNameLabel, c);
        c.gridx = 1;
        c.gridy = 0;
        this.add(clientNameField, c);
        c.gridx = 2;
        c.gridy = 0;
        this.add(clientIPLabel, c);
        c.gridx = 3;
        c.gridy = 0;
        this.add(clientIPAddressField, c);
        c.gridx = 4;
        c.gridy = 0;
        this.add(addClientButton, c);
    }
}
