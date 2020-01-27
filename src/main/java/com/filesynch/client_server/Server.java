package com.filesynch.client_server;

import com.filesynch.configuration.DataConfig;
import com.filesynch.dto.*;
import com.filesynch.repository.ClientInfoRepository;
import com.filesynch.repository.FileInfoRepository;
import com.filesynch.repository.FilePartRepository;
import com.filesynch.repository.TextMessageRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

@Component
public class Server extends UnicastRemoteObject implements ServerInt {
    @Getter
    @Setter
    private ServerStatus serverStatus;
    private ClientInfoRepository clientInfoRepository;
    private FileInfoRepository fileInfoRepository;
    private FilePartRepository filePartRepository;
    private TextMessageRepository textMessageRepository;
    private final int FILE_MULTIPLICITY = 10;
    private final String FILE_INPUT_DIRECTORY = "src/main/resources/in/";
    private final String FILE_OUTPUT_DIRECTORY = "src/main/resources/out/";
    private HashMap<String, ClientInt> clientIntHashMap = new HashMap<>();

    public Server() throws RemoteException {
        super();
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(DataConfig.class);
        ctx.refresh();
        clientInfoRepository = ctx.getBean(ClientInfoRepository.class);
        fileInfoRepository = ctx.getBean(FileInfoRepository.class);
        filePartRepository = ctx.getBean(FilePartRepository.class);
        textMessageRepository = ctx.getBean(TextMessageRepository.class);
        serverStatus = ServerStatus.SERVER_STANDBY_FULL;
    }

    public String loginToServer(ClientInt clientInt) {
        System.out.println("ddd");
        ClientInfo clientInfo = null;
        try {
            clientInfo = clientInt.getClientInfoFromClient();
            System.out.println(clientInfo);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        System.out.println("mmm");
        String login = "admin";
        clientInfo.setLogin(login);
        clientInfoRepository.save(clientInfo);
        clientIntHashMap.put(login, clientInt);
        return login;
    }

    public String sendAndReceiveTextMessageFromServer(String login, String message) {
        if (clientIsLoggedIn(login)) {
            TextMessage textMessage = new TextMessage();
            textMessage.setMessage(message);
            textMessage.setClient(clientInfoRepository.findByLogin(login));
            textMessageRepository.save(textMessage);
            System.out.println(textMessage);
            return "Message Received!";
        } else {
            return "You are not logged in!";
        }
    }

    public boolean sendFileInfoToServer(String login, FileInfo fileInfo) {
        if (clientIsLoggedIn(login)) {
            fileInfo.setClient(clientInfoRepository.findByLogin(login));
            fileInfoRepository.save(fileInfo);
            System.out.println(fileInfo);
            return true;
        } else {
            return false;
        }
    }

    public boolean sendFilePartToServer(String login, FilePart filePart) {
        if (clientIsLoggedIn(login)) {
            try {
                File file = new File(filePart.getFileInfo().getName() + FILE_INPUT_DIRECTORY);
                if (filePart.isFirst()) {
                    file.createNewFile();
                }
                FileOutputStream out = new FileOutputStream(file, true);
                out.write(filePart.getData(), 0, filePart.getLength());
                out.flush();
                out.close();
                filePartRepository.save(filePart);
                System.out.println(filePart);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    // calls here
    public boolean sendTextMessageToClient(String login, String message) {
        serverStatus = ServerStatus.SERVER_WORK;
        ClientInt clientInt = clientIntHashMap.get(login);
        if (clientInt == null) {
            System.out.println("Login not correct!");
            return false;
        }
        try {
            clientInt.sendTextMessageToClient(message);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
        System.out.println("ok");
        return true;
    }

    // this is cycle for sending file parts from client to server, it calls here
    public boolean sendFileToClient(String login, String filename) throws RemoteException {
        serverStatus = (ServerStatus.SERVER_WORK);
        ClientInt clientInt = clientIntHashMap.get(login);
        if (clientInt == null) {
            System.out.println("Login not correct!");
            return false;
        }
        try {
            String filePathname = FILE_OUTPUT_DIRECTORY + filename;
            File file = new File(filePathname);
            FileInputStream in = new FileInputStream(file);

            FileInfo fileInfo = new FileInfo();
            fileInfo.setName(filename);
            fileInfo.setSize(file.length());
            clientInt.sendFileInfoToClient(fileInfo);

            byte[] fileData = new byte[1024 * 1024];
            int fileLength = in.read(fileData);
            boolean step = true;
            while (fileLength > 0) {
                System.out.println(fileLength);
                FilePart filePart = new FilePart();
                if (step) {
                    filePart.setFirst(true);
                } else {
                    filePart.setFirst(false);
                    step = false;
                }
                filePart.setHashKey((long) fileData.hashCode());
                filePart.setFileInfo(fileInfo);
                filePart.setData(fileData);
                filePart.setLength(fileLength);
                filePart.setStatus(FilePartStatus.NOT_SENT);
                System.out.println(clientInt.sendFilePartToClient(filePart));
                // todo check for "true" from method sendFilePart()!!!!!!!!!!!!
                fileLength = in.read(fileData);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void start(int port) throws Exception {
        // todo start server
    }

    public void stop() {
        // todo stop server
    }

    private boolean clientIsLoggedIn(String login) {
        ClientInfo client = clientInfoRepository.findByLogin(login);
        if (client == null) {
            return false;
        }
        return true;
    }
}
