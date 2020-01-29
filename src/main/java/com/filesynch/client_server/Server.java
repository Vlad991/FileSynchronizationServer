package com.filesynch.client_server;

import com.filesynch.configuration.DataConfig;
import com.filesynch.converter.ClientInfoConverter;
import com.filesynch.converter.FileInfoConverter;
import com.filesynch.converter.FilePartConverter;
import com.filesynch.converter.TextMessageConverter;
import com.filesynch.dto.*;
import com.filesynch.entity.ClientInfo;
import com.filesynch.entity.FileInfo;
import com.filesynch.entity.FilePart;
import com.filesynch.entity.TextMessage;
import com.filesynch.repository.ClientInfoRepository;
import com.filesynch.repository.FileInfoRepository;
import com.filesynch.repository.FilePartRepository;
import com.filesynch.repository.TextMessageRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.awt.*;
import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

public class Server extends UnicastRemoteObject implements ServerInt {
    @Getter
    @Setter
    private ServerStatus serverStatus;
    private ClientInfoConverter clientInfoConverter;
    private FileInfoConverter fileInfoConverter;
    private FilePartConverter filePartConverter;
    private TextMessageConverter textMessageConverter;
    private ClientInfoRepository clientInfoRepository;
    private FileInfoRepository fileInfoRepository;
    private FilePartRepository filePartRepository;
    private TextMessageRepository textMessageRepository;
    private final int FILE_PART_SIZE = 1; // in bytes (1 B)
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
        clientInfoConverter = new ClientInfoConverter();
        fileInfoConverter = new FileInfoConverter(clientInfoConverter);
        filePartConverter = new FilePartConverter(clientInfoConverter, fileInfoConverter);
        textMessageConverter = new TextMessageConverter(clientInfoConverter);
        serverStatus = ServerStatus.SERVER_STANDBY_FULL;
    }

    public String loginToServer(ClientInt clientInt) {
        ClientInfoDTO clientInfoDTO = null;
        try {
            clientInfoDTO = clientInt.getClientInfoFromClient();
            System.out.println(clientInfoDTO);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        String login = null;
        if (clientInfoDTO.getLogin() != null && clientInfoRepository.findByLogin(clientInfoDTO.getLogin()) != null) {
            login = clientInfoDTO.getLogin();
        } else {
            login = "admin";
            clientInfoDTO.setLogin(login);
            clientInfoRepository.save(clientInfoConverter.convertToEntity(clientInfoDTO));
        }
        clientIntHashMap.put(login, clientInt);
        return login;
    }

    public String sendAndReceiveTextMessageFromServer(String login, String message) {
        if (clientIsLoggedIn(login)) {
            TextMessage textMessage = new TextMessage();
            textMessage.setMessage(message);
            textMessage.setClient(clientInfoRepository.findByLogin(login));
            textMessageRepository.save(textMessage);
            System.out.println(textMessage.getMessage());
            return "Message Received!";
        } else {
            return "You are not logged in!";
        }
    }

    public boolean sendFileInfoToServer(String login, FileInfoDTO fileInfoDTO) {
        if (clientIsLoggedIn(login)) {
            FileInfo fileInfo = fileInfoConverter.convertToEntity(fileInfoDTO);
            fileInfo.setClient(clientInfoRepository.findByLogin(login));
            fileInfoRepository.save(fileInfo);
            System.out.println(fileInfoDTO);
            return true;
        } else {
            return false;
        }
    }

    public boolean sendFilePartToServer(String login, FilePartDTO filePartDTO) {
        if (clientIsLoggedIn(login)) {
            try {
                File file = new File(FILE_INPUT_DIRECTORY + filePartDTO.getFileInfoDTO().getName());
                if (filePartDTO.isFirst()) {
                    file.createNewFile();
                }
                FileOutputStream out = new FileOutputStream(file, true);
                out.write(filePartDTO.getData(), 0, filePartDTO.getLength());
                out.flush();
                out.close();
                FilePart filePart = filePartConverter.convertToEntity(filePartDTO);
                filePart.setClient(clientInfoRepository.findByLogin(login));
                FileInfo fileInfo = fileInfoRepository.findByNameAndSizeAndClient(
                        filePart.getFileInfo().getName(),
                        filePart.getFileInfo().getSize(),
                        clientInfoRepository.findByLogin(login));
                if (fileInfo != null) {
                    filePart.setFileInfo(fileInfo);
                }
                filePartRepository.save(filePart);
                //System.out.println(filePartDTO);
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
        setServerStatus(ServerStatus.SERVER_WORK);
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
    public boolean sendFileToClient(String login, String filename) {
        setServerStatus(ServerStatus.SERVER_WORK);
        ClientInt clientInt = clientIntHashMap.get(login);
        if (clientInt == null) {
            System.out.println("Login not correct!");
            return false;
        }
        try {
            String filePathname = FILE_OUTPUT_DIRECTORY + filename;
            File file = new File(filePathname);
            FileInputStream in = new FileInputStream(file);

            FileInfoDTO fileInfoDTO = new FileInfoDTO();
            fileInfoDTO.setName(filename);
            fileInfoDTO.setSize(file.length());
            ClientInfoDTO clientInfoDTO = clientInfoConverter
                    .convertToDto(clientInfoRepository.findByLogin(login));
            fileInfoDTO.setClient(clientInfoDTO);
            clientInt.sendFileInfoToClient(fileInfoDTO);

            byte[] fileData = new byte[FILE_PART_SIZE];
            int fileLength = in.read(fileData);
            boolean step = true;
            while (fileLength > 0) {
                System.out.println(fileLength);
                FilePartDTO filePartDTO = new FilePartDTO();
                if (step) {
                    filePartDTO.setFirst(true);
                } else {
                    filePartDTO.setFirst(false);
                    step = false;
                }
                filePartDTO.setHashKey((long) fileData.hashCode());
                filePartDTO.setFileInfoDTO(fileInfoDTO);
                filePartDTO.setData(fileData);
                filePartDTO.setLength(fileLength);
                filePartDTO.setStatus(FilePartStatus.NOT_SENT);
                filePartDTO.setClient(clientInfoDTO);
                System.out.println(clientInt.sendFilePartToClient(filePartDTO));
                // todo check for "true" from method sendFilePart()!!!!!!!!!!!!
                fileLength = in.read(fileData);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void start(int port) {
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

    private void setServerStatus(ServerStatus status) {
        serverStatus = status;
    }
}
