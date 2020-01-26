package com.filesynch.gui;

import com.filesynch.dao.ClientInfoDAO;
import com.filesynch.dto.ClientInfo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class TableBuilder {
    private ClientInfoDAO clientInfoDAO;
    private JTable clientsTable;
    private DefaultTableModel model;

    public TableBuilder(JTable clientsTable, DefaultTableModel model) {
        this.clientInfoDAO = new ClientInfoDAO();
        this.model = model;
        this.clientsTable = clientsTable;
        clientsTable.setModel(model);
    }

    public void updateClientsTable() {
        List<ClientInfo> clientInfoList = clientInfoDAO.getClientInfoList();
        model.setRowCount(0);
        for (ClientInfo clientInfo : clientInfoList) {
            model.addRow(new Object[]{ clientInfo.getIpAddress(), clientInfo.getName(), clientInfo.getStatus()}); //todo
        }
    }
}
