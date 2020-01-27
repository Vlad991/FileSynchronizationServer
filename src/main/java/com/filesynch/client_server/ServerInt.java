package com.filesynch.client_server;

import com.filesynch.dto.FileInfo;
import com.filesynch.dto.FilePart;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInt extends Remote {
    public String loginToServer(ClientInt clientInt) throws RemoteException; // try to connect and receive login-name
    public String sendAndReceiveTextMessageFromServer(String login, String message) throws RemoteException;
    public boolean sendFileInfoToServer(String login, FileInfo fileInfo) throws RemoteException;
    public boolean sendFilePartToServer(String login, FilePart filePart) throws RemoteException; // cycle for sending all file parts is on Client (class)
}
