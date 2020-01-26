package com.filesynch.dao;

import com.filesynch.dto.ClientInfo;
import com.filesynch.dto.ClientStatus;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClientInfoDAO extends Util {

    public List<ClientInfo> getClientInfoList() {
        List<ClientInfo> clientInfoList = new ArrayList<>();
        PreparedStatement ps;
        try {
            ps = getPreparedStatement("select * from Clients");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ClientInfo clientInfo = new ClientInfo();
                clientInfo.setIpAddress(rs.getString("ip_address"));
                clientInfo.setName(rs.getString("name"));
                clientInfo.setStatus(ClientStatus.valueOf(rs.getString("status")));
                clientInfoList.add(clientInfo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return clientInfoList;
    }

    public void registerNewClient(ClientInfo clientInfo) {
        clientInfo.setStatus(ClientStatus.DISCONNECTED);
        addClientInfo(clientInfo);
    }

    public ClientInfo getClientInfoByIpAddress(String ipAddress) {
        ClientInfo clientInfo = null;
        PreparedStatement ps;
        try {
            ps = getPreparedStatement("select * from Clients where ip_address = ?");
            ps.setString(1, ipAddress);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                clientInfo = new ClientInfo();
                clientInfo.setIpAddress(ipAddress);
                clientInfo.setName(rs.getString("name"));
                clientInfo.setStatus(ClientStatus.valueOf(rs.getString("status")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return clientInfo;
    }

    private void addClientInfo(ClientInfo clientInfo) {
        PreparedStatement ps = null;
        try {
            ps = getPreparedStatement("insert into Clients " +
                    "(ip_address, " +
                    "name, " +
                    "status) " +
                    "values (?, ?, ?)");
            ps.setString(1, clientInfo.getIpAddress());
            ps.setString(2, clientInfo.getName());
            ps.setString(3, clientInfo.getStatus().getStatus());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setClientInfoStatus(String ipAddress, ClientStatus status) {
        PreparedStatement ps = null;
        try {
            ps = getPreparedStatement("update Clients set " +
                    "status = ? " +
                    "where ip_address = ?");
            ps.setString(1, status.getStatus());
            ps.setString(2, ipAddress);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
