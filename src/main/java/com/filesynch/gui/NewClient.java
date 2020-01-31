package com.filesynch.gui;

import com.filesynch.Main;
import com.filesynch.client_server.ClientInt;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class NewClient {
    @Getter
    private JPanel jPanelMain;
    private JTextField jTextFieldIP;
    private JTextField jTextFieldPort;
    private JTextField jTextFieldAddress;
    @Getter
    private JButton jButtonConnect;
    private JLabel jLabelIP;
    private JLabel jLabelPort;
    private JLabel jLabelAddress;
    @Getter
    private JTextField jTextFieldLogin;
    private JButton jButtonReject;
    @Getter
    @Setter
    private ClientInt clientInt;

    public NewClient() {
        jButtonConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                synchronized (clientInt) {
                    clientInt.notify();
                }
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("ConnectToServer");
        frame.setContentPane(new NewClient().jPanelMain);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
