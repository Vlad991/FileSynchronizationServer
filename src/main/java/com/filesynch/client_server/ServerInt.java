package com.filesynch.client_server;

import com.filesynch.dto.FileInfoDTO;
import com.filesynch.dto.FilePartDTO;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInt extends Remote {
    public String loginToServer(ClientInt clientInt) throws RemoteException; // try to connect and receive login-name
    public String sendAndReceiveTextMessageFromServer(String login, String message) throws RemoteException;
    public boolean sendFileInfoToServer(String login, FileInfoDTO fileInfoDTO) throws RemoteException;
    public boolean sendFilePartToServer(String login, FilePartDTO filePartDTO) throws RemoteException; // cycle for sending all file parts is on Client (class)
}
