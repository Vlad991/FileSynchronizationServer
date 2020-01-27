package com.filesynch.repository;

import com.filesynch.dto.ClientInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientInfoRepository extends JpaRepository<ClientInfo, Long> {
    ClientInfo findByLogin(String login);
}
