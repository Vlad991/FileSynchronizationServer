package com.filesynch.server;

import com.filesynch.Main;
import com.filesynch.dao.ClientInfoDAO;
import com.filesynch.dto.ClientInfo;
import com.filesynch.dto.ClientStatus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Server {
    private ServerSocket server;
    private boolean serverStatus = false;
    private ClientInfoDAO clientInfoDAO;

    public Server() {
    }

    public void start(int port) throws Exception {
        if (isRunning()) {
            System.out.println("Server is already running!");
            return;
        }
        serverStatus = true;
        server = new ServerSocket(port);
        System.out.println("Server started on port(" + server.getLocalPort() + ")");

        Runnable acceptClientsRunnable = () -> {
            try {
                acceptClients();
            } catch (Exception e) {
                System.out.println("Server stopped!");
            }
        };
        new Thread(acceptClientsRunnable).start();
    }

    private void acceptClients() throws Exception {
        while (isRunning()) {
            Socket socket = server.accept();

            Runnable clientRequestProcessingRunnable = () -> {
                try {
                    processQueries(socket);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
            new Thread(clientRequestProcessingRunnable).start();
        }
        System.out.println("Server is stopped");
    }

    private void processQueries(Socket socket) throws Exception {
        ClientInfo clientInfo = clientInfoDAO
                .getClientInfoByIpAddress(socket.getInetAddress().toString().replace("/", ""));
        if (clientInfo == null) {
            System.out.println("Client " + socket.getInetAddress().toString().replace("/", "") + " is not registered!");
        } else {
            clientInfoDAO.setClientInfoStatus(clientInfo.getIpAddress(), ClientStatus.CONNECTED);
            clientInfo = clientInfoDAO
                    .getClientInfoByIpAddress(socket.getInetAddress().toString().replace("/", ""));
            Main.getMainGUI().getClientsTablePanel().getTableBuilder().updateClientsTable();
            System.out.println("Client accepted: " + socket.toString());
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            while (processQuery(inputStream, printWriter, clientInfo)) {
                if (!isRunning()) break;
                if (clientInfo.getStatus() == ClientStatus.DISCONNECTED) {
                    clientInfoDAO.setClientInfoStatus(clientInfo.getIpAddress(), ClientStatus.DISCONNECTED);
                    break;
                }
            }

            System.out.println("Client " + socket.toString() + " disconnected!");
            Main.getMainGUI().getClientsTablePanel().getTableBuilder().updateClientsTable();
        }
    }

    private boolean processQuery(BufferedReader inputStream, PrintWriter printWriter, ClientInfo clientInfo) {
        try {
            String jsonFileData;
            jsonFileData = inputStream.readLine();
            System.out.println(jsonFileData);
            if (jsonFileData == null) {
                clientInfo.setStatus(ClientStatus.DISCONNECTED);
                return true;
            }
            if (jsonFileData.equals("disconnect") || jsonFileData.equals("-1")) {
                clientInfo.setStatus(ClientStatus.DISCONNECTED);
                return true;
            }
            ArrayList<HashMap> fileDataList = objectMapper.readValue(jsonFileData, ArrayList.class);

            if (fileDataList.size() == 0) {
                printWriter.println("Json string is incorrect!");
                return true;
            }
            if (fileDataList.get(0).get(QueryParameter.iEventType.getParameter()) == null) {
                printWriter.println("Json has no eventType field!");
                return true;
            }
            for (HashMap fileDataElement : fileDataList) {
                FileData fileData = new FileData();
                fileData.setEventType((Integer) fileDataElement.get(QueryParameter.iEventType.getParameter()));
                HashMap<String, Object> jsonHeaderHashMap = new HashMap<>();
                HashMap<String, Object> jsonParameterHashMap;
                for (QueryParameter parameter : QueryParameter.values()) {
                    Object fileDataElementFieldValue = fileDataElement.get(parameter.getParameter());
                    if (fileDataElementFieldValue instanceof String) {
                        fileDataElementFieldValue = fileDataElementFieldValue.toString().replaceAll("'", "");
                    }
                    jsonHeaderHashMap.put(parameter.toString(), fileDataElementFieldValue);
                    fileDataElement.remove(parameter.getParameter());
                }
                fileDataElement.forEach((key, value) -> {
                    value = value.toString().replaceAll("'", "");
                    fileDataElement.put(key, value);
                });
                jsonParameterHashMap = fileDataElement;
                fileData.setJsonHeaderHashMap(jsonHeaderHashMap);
                fileData.setJsonParameterHashMap(jsonParameterHashMap);
                fileDataDAO.addFileData(fileData);
            }

            printWriter.println("Operation success!");
            return true;
        } catch (Exception ex) {
//            ex.printStackTrace();
            printWriter.println("Not a json format!");
            return true;
        }
    }

    public boolean isRunning() {
        return serverStatus;
    }

    public void stop() throws IOException {
        serverStatus = false;
        List<ClientInfo> clientInfoList = clientInfoDAO.getClientInfoList();
        for (ClientInfo client : clientInfoList) {
            clientInfoDAO.setClientInfoStatus(client.getIpAddress(), ClientStatus.DISCONNECTED);
        }
        if (server != null) {
            server.close();
        }
        Main.getMainGUI().getClientsTablePanel().getTableBuilder().updateClientsTable();
    }
}
