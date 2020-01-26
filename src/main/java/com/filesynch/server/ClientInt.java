package com.filesynch.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientInt extends Remote {
    public boolean sendData(String filename, byte[] data, int length) throws RemoteException;
    public String getName() throws RemoteException;
}
