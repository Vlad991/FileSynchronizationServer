package com.filesynch.gui;

import javax.swing.*;
import java.awt.*;

public class LogoImagePanel extends JPanel {

    private ImageIcon image;
    private JLabel imageLabel;

    public LogoImagePanel() {
        image = new ImageIcon(getClass().getResource("/img/logo.jpg"));
        imageLabel = new JLabel(image);

        this.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        this.add(imageLabel, c);
    }
}
