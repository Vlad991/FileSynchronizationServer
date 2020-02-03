package com.filesynch.gui;

import com.filesynch.Main;
import com.filesynch.dto.ServerStatus;
import com.filesynch.entity.ClientInfo;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FileSynchronizationServer {
    @Getter
    private JPanel jPanelServer;
    private JTabbedPane tabbedPane1;
    private JTable table2;
    private JTable table3;
    private JPanel jPanelMain;
    private JPanel jPanelTextMessage;
    private JPanel jPanelLog;
    private JPanel jPanelFile;
    private JPanel jPanelCommand;
    private JTextField jTextFieldTextMessage;
    private JButton jButtonTextMessage;
    private JLabel jLabelTextMessage;
    private JTextField jTextFieldFile;
    private JButton jButtonSendFile;
    private JTextField jTextFieldCommand;
    private JButton jButtonSendCommand;
    private JLabel jLabelFileTitle;
    private JLabel jLabelFile;
    private JLabel jLabelCommand;
    @Getter
    private JProgressBar jProgressBarFile;
    @Getter
    private JTextArea jTextAreaLog;
    private JButton jButtonStartServer;
    private JButton jButtonStopServer;
    @Getter
    private JList jListQueueSending;
    @Getter
    @Setter
    private JList jListClientList;
    private JPanel jPanelClientInfo;
    private JLabel jLabelClientStatus;
    private JLabel jLabelClientInfo;
    private JLabel jLabelPCModel;
    private JLabel jLabelIP;
    private JLabel jLabelPCName;
    private JLabel jLabelClientStatusValue;
    private JLabel jLabelIPValue;
    private JLabel jLabelPCNameValue;
    private JLabel jLabelPCModelValue;
    private JLabel jLabelServerInfo;
    private JPanel jPanelServerInfo;
    private JLabel jLabelServerStatus;
    private JLabel jLabelServerInfoValue;
    private JLabel jLabelServerStatusValue;
    private JLabel jLabelLog;
    private JScrollPane jScrollPaneLog;
    private JScrollPane jScrollPaneQueueSending;
    private JScrollPane jScrollPaneClientList;
    private JButton jButtonSendAllFiles;
    private JScrollPane jScrollPaneQueueReceiving;
    @Getter
    private JList jListQueueReceiving;

    public FileSynchronizationServer() {
        jButtonStopServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Main.stopServer(1099, "fs");
                Main.server.setServerStatus(ServerStatus.SERVER_STANDBY_FULL);
                jLabelServerStatusValue.setText(ServerStatus.SERVER_STANDBY_FULL.getStatus());
                jLabelServerStatusValue.setForeground(Color.RED);
            }
        });
        jButtonStartServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Main.startServer(1099, "fs");
                Main.server.setServerStatus(ServerStatus.SERVER_WORK);
                jLabelServerStatusValue.setText(ServerStatus.SERVER_WORK.getStatus());
                jLabelServerStatusValue.setForeground(Color.GREEN);
            }
        });
        jListClientList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                ClientInfo clientInfo =
                        Main.server.getClientInfoRepository()
                                .findByLogin(jListClientList.getSelectedValue().toString());
                jLabelClientStatusValue.setText(clientInfo.getStatus().getStatus());
                jLabelIPValue.setText(clientInfo.getIpAddress());
                jLabelPCNameValue.setText(clientInfo.getPcName());
                jLabelPCModelValue.setText(clientInfo.getPcModel());
            }
        });
        jButtonTextMessage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(() -> {
                    Main.sendMessage(jListClientList.getSelectedValue().toString(), jTextFieldTextMessage.getText());
                }).start();
            }
        });
        jButtonSendFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(() -> {
                    Main.sendFile(jListClientList.getSelectedValue().toString(), jTextFieldFile.getText());
                }).start();
            }
        });
        jButtonSendAllFiles.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(() -> {
                    Main.sendAllFiles(jListClientList.getSelectedValue().toString());
                }).start();
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("File Synchronization Client");
        frame.setContentPane(new FileSynchronizationServer().jPanelServer);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
