package com.filesynch.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInt extends Remote {
    public boolean login(ClientInt clientInt) throws RemoteException;
}
