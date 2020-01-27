package com.filesynch.client_server;

import com.filesynch.dto.ClientInfo;
import com.filesynch.dto.FileInfo;
import com.filesynch.dto.FilePart;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientInt extends Remote {
    public boolean sendLoginToClient(String login) throws RemoteException;
    public ClientInfo getClientInfoFromClient() throws RemoteException;
    public void sendTextMessageToClient(String message) throws RemoteException;
    public boolean sendCommandToClient(String command) throws RemoteException; // TODO send command but receive smth
    public boolean sendFileInfoToClient(FileInfo fileInfo) throws RemoteException;
    public boolean sendFilePartToClient(FilePart filePart) throws RemoteException; // cycle for sending all file parts is on Server (class)
}
