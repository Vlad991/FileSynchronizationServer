package com.filesynch.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ReadPropertyFile {
    private Properties properties;
    private String propertyFilePath;
    private String dbDriverClassName;
    private String dbUrl;

    public ReadPropertyFile(String propertyFilePath) throws IOException {
        this.propertyFilePath = propertyFilePath;
        this.properties = new Properties();
        InputStream inputStream =
                ReadPropertyFile.class
                        .getClassLoader()
                        .getResourceAsStream(this.propertyFilePath);
        properties.load(inputStream);
        this.dbDriverClassName = properties.getProperty("db.driverClassName");
        this.dbUrl = properties.getProperty("db.url");
    }

    public String getDbDriverClassName() {
        return dbDriverClassName;
    }

    public String getDbUrl() {
        return dbUrl;
    }
}
