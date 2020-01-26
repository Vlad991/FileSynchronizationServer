package com.filesynch.dao;

import com.filesynch.properties.ReadPropertyFile;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Util {
    private static ReadPropertyFile propertyFile;
    private static Connection connection;

    static {
        try {
            propertyFile = new ReadPropertyFile("application.properties");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final String DB_DRIVER = propertyFile.getDbDriverClassName();
    private static final String DB_URL = propertyFile.getDbUrl();

    public Util() {
        getConnection();
    }

    public PreparedStatement getPreparedStatement(String sql) {
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ps;
    }

    public Connection getConnection() {
        if (connection == null) {
            try {
                Class.forName(DB_DRIVER);
                connection = DriverManager.getConnection(DB_URL);
                System.out.println("Connection to Database is OK");
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }
        }
        return connection;
    }
}
