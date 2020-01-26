package com.filesynch;

import com.filesynch.gui.MainGUI;

import java.io.IOException;

public class Main {
    private static MainGUI app;

    public static void main(String[] args) throws IOException {
        app = new MainGUI();
        app.setVisible(true);
    }

    public static MainGUI getMainGUI() {
        return app;
    }
}
