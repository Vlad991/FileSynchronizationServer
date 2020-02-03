package com.filesynch.client_server;

import com.filesynch.dto.ClientInfoDTO;
import com.filesynch.dto.FileInfoDTO;
import com.filesynch.dto.FilePartDTO;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientInt extends Remote {
    public boolean sendLoginToClient(String login) throws RemoteException;
    public ClientInfoDTO getClientInfoFromClient() throws RemoteException;
    public void sendTextMessageToClient(String message) throws RemoteException;
    public boolean sendCommandToClient(String command) throws RemoteException; // TODO send command but receive smth
    public boolean sendFileInfoToClient(FileInfoDTO fileInfoDTO) throws RemoteException;
    public FilePartDTO getFirstNotSentFilePartFromClient(FileInfoDTO fileInfoDTO) throws RemoteException;
    public boolean sendFilePartToClient(FilePartDTO filePartDTO) throws RemoteException; // cycle for sending all file parts is on Server (class)
}
