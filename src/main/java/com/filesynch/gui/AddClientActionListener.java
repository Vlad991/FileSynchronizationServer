package com.filesynch.gui;

import com.filesynch.dao.ClientInfoDAO;
import com.filesynch.dto.ClientInfo;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class AddClientActionListener implements MouseListener {
    private ClientInfoDAO clientInfoDAO;
    private JTextField clientNameField;
    private JTextField clientIPAddressField;
    private TableBuilder tableBuilder;

    public AddClientActionListener(JTextField clientNameField,
                                   JTextField clientIPAddressField,
                                   TableBuilder tableBuilder) {
        this.clientInfoDAO = new ClientInfoDAO();
        this.clientNameField = clientNameField;
        this.clientIPAddressField = clientIPAddressField;
        this.tableBuilder = tableBuilder;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        ClientInfo clientInfo = new ClientInfo();
        clientInfo.setName(clientNameField.getText());
        clientInfo.setIpAddress(clientIPAddressField.getText());
        clientInfoDAO.registerNewClient(clientInfo);
        tableBuilder.updateClientsTable();
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
