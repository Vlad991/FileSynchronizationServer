package com.filesynch.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ClientsTablePanel extends JPanel {
    private JLabel clientsLabel = new JLabel("Clients: ");
    private JTable clientsTable = new JTable();
    private TableBuilder tableBuilder;

    public ClientsTablePanel() {
        createTable();

        JScrollPane scrollPane = new JScrollPane(clientsTable);

        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        this.add(clientsLabel, c);
        c.gridx = 0;
        c.gridy = 1;
        this.add(scrollPane, c);
    }

    private void createTable() {
        DefaultTableModel model = new DefaultTableModel(new String[]{"IP-Address", "Name", "Status"}, 0);
        clientsTable.setModel(model);
        tableBuilder = new TableBuilder(clientsTable, model);
        tableBuilder.updateClientsTable();
    }

    public TableBuilder getTableBuilder() {
        return tableBuilder;
    }
}
