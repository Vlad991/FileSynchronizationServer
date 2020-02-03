package com.filesynch.client_server;

import com.filesynch.Main;
import com.filesynch.configuration.DataConfig;
import com.filesynch.converter.*;
import com.filesynch.dto.*;
import com.filesynch.entity.*;
import com.filesynch.gui.NewClient;
import com.filesynch.repository.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Server extends UnicastRemoteObject implements ServerInt {
    @Getter
    @Setter
    private ServerStatus serverStatus;
    private ClientInfoConverter clientInfoConverter;
    private FileInfoReceivedConverter fileInfoReceivedConverter;
    private FileInfoSentConverter fileInfoSentConverter;
    private FilePartReceivedConverter filePartReceivedConverter;
    private FilePartSentConverter filePartSentConverter;
    private TextMessageConverter textMessageConverter;
    @Getter
    private ClientInfoRepository clientInfoRepository;
    @Getter
    private FileInfoReceivedRepository fileInfoReceivedRepository;
    @Getter
    private FileInfoSentRepository fileInfoSentRepository;
    @Getter
    private FilePartReceivedRepository filePartReceivedRepository;
    @Getter
    private FilePartSentRepository filePartSentRepository;
    private TextMessageRepository textMessageRepository;
    private final int FILE_PART_SIZE = 1; // in bytes (1 B)
    public final String FILE_INPUT_DIRECTORY = "src/main/resources/in/";
    public final String FILE_OUTPUT_DIRECTORY = "src/main/resources/out/";
    @Getter
    private HashMap<String, ClientInt> clientIntHashMap = new HashMap<>();
    @Setter
    @Getter
    private Logger logger;
    @Setter
    private JProgressBar fileProgressBar;


    public Server() throws RemoteException {
        super();
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(DataConfig.class);
        ctx.refresh();
        clientInfoRepository = ctx.getBean(ClientInfoRepository.class);
        fileInfoReceivedRepository = ctx.getBean(FileInfoReceivedRepository.class);
        fileInfoSentRepository = ctx.getBean(FileInfoSentRepository.class);
        filePartReceivedRepository = ctx.getBean(FilePartReceivedRepository.class);
        filePartSentRepository = ctx.getBean(FilePartSentRepository.class);
        textMessageRepository = ctx.getBean(TextMessageRepository.class);
        clientInfoConverter = new ClientInfoConverter();
        fileInfoReceivedConverter = new FileInfoReceivedConverter(clientInfoConverter);
        fileInfoSentConverter = new FileInfoSentConverter(clientInfoConverter);
        filePartReceivedConverter = new FilePartReceivedConverter(clientInfoConverter, fileInfoReceivedConverter);
        filePartSentConverter = new FilePartSentConverter(clientInfoConverter, fileInfoSentConverter);
        textMessageConverter = new TextMessageConverter(clientInfoConverter);
        serverStatus = ServerStatus.SERVER_STANDBY_FULL;
    }

    public String loginToServer(ClientInt clientInt) {
        ClientInfoDTO clientInfoDTO = null;
        try {
            clientInfoDTO = clientInt.getClientInfoFromClient();
            logger.log(clientInfoDTO.toString());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        String login = null;
        if (clientInfoDTO.getLogin() != null && clientInfoRepository.findByLogin(clientInfoDTO.getLogin()) != null) {
            login = clientInfoDTO.getLogin();
        } else {
            NewClient newClient = new NewClient();
            JFrame newClientFrame = Main.showNewClientIcon(clientInt, newClient);
            synchronized (clientInt) {
                try {
                    clientInt.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            login = newClient.getJTextFieldLogin().getText();
            clientInfoDTO.setLogin(login);
            clientInfoRepository.save(clientInfoConverter.convertToEntity(clientInfoDTO));
            Main.hideNewClientIcon(newClientFrame);
        }
        clientIntHashMap.put(login, clientInt);
        Main.updateClientList();
        File directory = new File(FILE_INPUT_DIRECTORY + login);
        if (!directory.exists()) {
            directory.mkdir();
        }
        return login;
    }

    public String sendAndReceiveTextMessageFromServer(String login, String message) {
        if (clientIsLoggedIn(login)) {
            TextMessage textMessage = new TextMessage();
            textMessage.setMessage(message);
            textMessage.setClient(clientInfoRepository.findByLogin(login));
            textMessageRepository.save(textMessage);
            logger.log(textMessage.getMessage());
            return "Message Received!";
        } else {
            return "You are not logged in!";
        }
    }

    public boolean sendFileInfoToServer(String login, FileInfoDTO fileInfoDTO) {
        if (clientIsLoggedIn(login)) {
            FileInfoReceived fileInfo = fileInfoReceivedConverter.convertToEntity(fileInfoDTO);
            fileInfo.setClient(clientInfoRepository.findByLogin(login));
            fileInfoReceivedRepository.save(fileInfo);
            logger.log(fileInfoDTO.toString());
            Main.updateFileQueue();
            return true;
        } else {
            return false;
        }
    }

    public boolean sendFilePartToServer(String login, FilePartDTO filePartDTO) {
        if (clientIsLoggedIn(login)) {
            try {
                File file = new File(FILE_INPUT_DIRECTORY + login + "/" + filePartDTO.getFileInfoDTO().getName());
                if (filePartDTO.getOrder() == 1) {
                    file.createNewFile();
                }
                FileOutputStream out = new FileOutputStream(file, true);
                out.write(filePartDTO.getData(), 0, filePartDTO.getLength());
                out.flush();
                out.close();
                filePartDTO.setStatus(FilePartStatus.SENT);
                FilePartReceived filePart = filePartReceivedConverter.convertToEntity(filePartDTO);
                filePart.setClient(clientInfoRepository.findByLogin(login));
                FileInfoReceived fileInfo = fileInfoReceivedRepository.findByNameAndSizeAndClient(
                        filePart.getFileInfo().getName(),
                        filePart.getFileInfo().getSize(),
                        clientInfoRepository.findByLogin(login));
                if (fileInfo != null) {
                    filePart.setFileInfo(fileInfo);
                }
                filePartReceivedRepository.save(filePart);
                Main.updateFileQueue();
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
            logger.log("Login not correct");
            return false;
        }
        try {
            clientInt.sendTextMessageToClient(message);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
        logger.log("Message sent");
        return true;
    }

    // this is cycle for sending file parts from client to server, it calls here
    public boolean sendFileToClientFast(String login, String filename) {
        setServerStatus(ServerStatus.SERVER_WORK);
        ClientInt clientInt = clientIntHashMap.get(login);
        if (clientInt == null) {
            logger.log("Login not correct");
            return false;
        }
        try {
            String filePathname = FILE_OUTPUT_DIRECTORY + filename;
            File file = new File(filePathname);
            FileInputStream in = new FileInputStream(file);

            FileInfoDTO fileInfoDTO = new FileInfoDTO();
            fileInfoDTO.setName(filename);
            fileInfoDTO.setSize(file.length());
            ClientInfo clientInfo = clientInfoRepository.findByLogin(login);
            ClientInfoDTO clientInfoDTO = clientInfoConverter.convertToDto(clientInfo);
            fileInfoDTO.setClient(clientInfoDTO);
            clientInt.sendFileInfoToClient(fileInfoDTO);
            FileInfoSent fileInfo = fileInfoSentConverter.convertToEntity(fileInfoDTO);
            fileInfo.setClient(clientInfo);
            fileInfo = fileInfoSentRepository.save(fileInfo);
            Main.updateFileQueue();

            byte[] fileData = new byte[FILE_PART_SIZE];
            int fileLength = in.read(fileData);
            int step = 1;
            fileProgressBar.setMinimum(0);
            fileProgressBar.setMaximum((int) fileInfoDTO.getSize());
            int progressValue = 0;
            while (fileLength > 0) {
                logger.log(String.valueOf(fileLength));
                FilePartDTO filePartDTO = new FilePartDTO();
                filePartDTO.setOrder(step);
                step++;
                filePartDTO.setHashKey((long) filePartDTO.hashCode());
                filePartDTO.setFileInfoDTO(fileInfoDTO);
                filePartDTO.setData(fileData);
                filePartDTO.setLength(fileLength);
                filePartDTO.setStatus(FilePartStatus.NOT_SENT);
                filePartDTO.setClient(clientInfoDTO);
                logger.log(String.valueOf(clientInt.sendFilePartToClient(filePartDTO)));
                // todo check for "true" from method sendFilePart()!!!!!!!!!!!!
                FilePartSent filePartSent = filePartSentConverter.convertToEntity(filePartDTO);
                filePartSent.setClient(clientInfo);
                filePartSent.setFileInfo(fileInfo);
                filePartSentRepository.save(filePartSent);

                fileLength = in.read(fileData);
                progressValue += FILE_PART_SIZE;
                fileProgressBar.setValue(progressValue);
                Thread.sleep(2000);
            }
            Main.updateFileQueue();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean sendAllFilesToClient(String login) {
        setServerStatus(ServerStatus.SERVER_WORK);
        ClientInt clientInt = clientIntHashMap.get(login);
        if (clientInt == null) {
            logger.log("Login not correct");
            return false;
        }
        try (Stream<Path> walk = Files.walk(Paths.get(FILE_OUTPUT_DIRECTORY
                .substring(0, FILE_OUTPUT_DIRECTORY.length() - 1)))) {
            List<String> filePathNames = walk.filter(Files::isRegularFile)
                    .map(x -> x.toString()).collect(Collectors.toList());
            for (String filePath : filePathNames) {
                boolean result = sendFileToClient(login, filePath.replace(FILE_OUTPUT_DIRECTORY, ""));
                if (!result) {
                    return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean sendFileToClient(String login, String filename) {
        setServerStatus(ServerStatus.SERVER_WORK);
        ClientInt clientInt = clientIntHashMap.get(login);
        if (clientInt == null) {
            logger.log("Login not correct");
            return false;
        }
        File file = new File(FILE_OUTPUT_DIRECTORY + filename);
        if (!file.exists()) {
            logger.log("File " + filename + " not exists");
            return false;
        }
        LinkedHashMap<Integer, FilePartDTO> filePartHashMap = new LinkedHashMap<>();
        FileInfoSent fileInfo = null;
        FileInfoDTO fileInfoDTO = null;
        try {
            FileInputStream in = new FileInputStream(file);
            fileInfo = fileInfoSentRepository
                    .findByNameAndSizeAndClient_Login(filename, file.length(), login);
            if (fileInfo == null) {
                fileInfoDTO = new FileInfoDTO();
                fileInfoDTO.setName(filename);
                fileInfoDTO.setSize(file.length());
                ClientInfo clientInfo = clientInfoRepository.findByLogin(login);
                ClientInfoDTO clientInfoDTO = clientInfoConverter.convertToDto(clientInfo);
                fileInfoDTO.setClient(clientInfoDTO);
                fileInfo = fileInfoSentConverter.convertToEntity(fileInfoDTO);
                fileInfo.setClient(clientInfo);
                fileInfo.setFileStatus(FileStatus.NOT_TRANSFERRED);
                fileInfoSentRepository.save(fileInfo);
                Main.updateFileQueue();
            }
            fileInfoDTO = fileInfoSentConverter.convertToDto(fileInfo);
            ClientInfo clientInfo = clientInfoRepository.findByLogin(login);
            ClientInfoDTO clientInfoDTO = clientInfoConverter.convertToDto(clientInfo);

            byte[] fileData = new byte[FILE_PART_SIZE];
            int fileLength = in.read(fileData);
            int step = 1;
            while (fileLength > 0) {
                FilePartDTO filePartDTO = new FilePartDTO();
                filePartDTO.setOrder(step);
                step++;
                filePartDTO.setFileInfoDTO(fileInfoDTO);
                filePartDTO.setData(fileData);
                filePartDTO.setLength(fileLength);
                filePartDTO.setStatus(FilePartStatus.NOT_SENT);
                filePartDTO.setClient(clientInfoDTO);
                filePartDTO.setHashKey((long) filePartDTO.hashCode());
                filePartHashMap.put(step, filePartDTO);
                fileLength = in.read(fileData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        switch (fileInfo.getFileStatus()) {
            case NOT_TRANSFERRED:
                filePartSentRepository.deleteAllByClient_LoginAndFileInfo_Id(login, fileInfo.getId());
                filePartHashMap.forEach((order, filePartDTO) -> {
                    filePartSentRepository.save(filePartSentConverter.convertToEntity(filePartDTO));
                });
                return sendAllFilePartsToClient(filePartHashMap, fileInfoDTO, clientInt);
            case TRANSFER_PROCESS:
                return sendAllFilePartsToClient(filePartHashMap, fileInfoDTO, clientInt);
            case TRANSFERRED:
                return true;
        }
        Main.updateFileQueue();
        return true;
    }

    private boolean sendAllFilePartsToClient(LinkedHashMap<Integer, FilePartDTO> filePartHashMap,
                                             FileInfoDTO fileInfoDTO,
                                             ClientInt clientInt) {
        FileInfoSent fileInfo = fileInfoSentRepository
                .findByNameAndSizeAndClient_Login(
                        fileInfoDTO.getName(),
                        fileInfoDTO.getSize(),
                        fileInfoDTO.getClient().getLogin());
        fileInfo.setFileStatus(FileStatus.TRANSFER_PROCESS);
        fileInfo = fileInfoSentRepository.save(fileInfo);
        List<FilePartSent> filePartList = filePartSentRepository.findAllByFileInfo(fileInfo);
        Collections.sort(filePartList, new Comparator<FilePartSent>() {
            public int compare(FilePartSent o1, FilePartSent o2) {
                return Integer.compare(o1.getOrder(), o2.getOrder());
            }
        });
        FilePartSent firstNotSentFilePart = filePartList.stream()
                .filter(fp -> (fp.getStatus() == FilePartStatus.NOT_SENT))
                .findFirst()
                .get();
        FilePartDTO firstNotSentFilePartDTOFromClient = null;
        try {
            firstNotSentFilePartDTOFromClient =
                    clientInt.getFirstNotSentFilePartFromClient(fileInfoDTO);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
        FilePartSent firstNotSentFilePartFromClient =
                filePartSentConverter.convertToEntity(firstNotSentFilePartDTOFromClient);
        if (firstNotSentFilePart.getOrder() != firstNotSentFilePartFromClient.getOrder()) {
            if (firstNotSentFilePart.getOrder() > firstNotSentFilePartFromClient.getOrder()) {
                for (FilePartSent fp : filePartList) {
                    if (fp.getOrder() == (firstNotSentFilePart.getOrder() - 1)) {
                        fp.setStatus(FilePartStatus.NOT_SENT);
                        break;
                    }
                }
            } else {
                firstNotSentFilePart.setStatus(FilePartStatus.SENT);
            }
            sendAllFilePartsToClient(filePartHashMap, fileInfoDTO, clientInt);
        } else {
            fileProgressBar.setMinimum(0);
            fileProgressBar.setMaximum((int) fileInfoDTO.getSize());
            int progressValue = 0;
            for (int i = firstNotSentFilePart.getOrder(); i <= filePartHashMap.size(); i++) {
                try {
                    FilePartDTO filePartDTOToSend = filePartHashMap.get(i);
                    FilePartSent filePartToSend = filePartList.get(i - 1);
                    String filePathname = FILE_OUTPUT_DIRECTORY + filePartToSend.getFileInfo().getName();
                    File file = new File(filePathname);
                    FileInputStream in = new FileInputStream(file);
                    byte[] fileData = new byte[FILE_PART_SIZE];
                    int fileLength = in.read(fileData);

                    sendFilePartToClient(filePartDTOToSend, clientInt);

                    filePartToSend.setStatus(FilePartStatus.SENT);
                    filePartSentRepository.save(filePartToSend);
                    progressValue += FILE_PART_SIZE;
                    fileProgressBar.setValue(progressValue);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false; //todo? (break)
                }
            }
        }
        return true;
    }

    private boolean sendFilePartToClient(FilePartDTO filePartDTO, ClientInt clientInt) {
        boolean result = false;
        try {
            result = clientInt.sendFilePartToClient(filePartDTO);
            Thread.sleep(2000);
        } catch (InterruptedException | RemoteException e) {
            e.printStackTrace();
            return false;
        }
        logger.log(String.valueOf(result));
        if (result) {
            FilePartSent filePartSent = filePartSentRepository.findByHashKey(filePartDTO.getHashKey());
            filePartSent.setStatus(FilePartStatus.SENT);
            filePartSentRepository.save(filePartSent);
            return true;
        } else {
            return false;
        }
    }

    // calls here
//    public boolean sendCommandToClient(Command command) {
//        switch (command) {
//            case CONTINUE_SENDING:
//
//                break;
//        }
//    }

    private boolean clientIsLoggedIn(String login) {
        ClientInfo client = clientInfoRepository.findByLogin(login);
        if (client == null) {
            return false;
        }
        return true;
    }
}
