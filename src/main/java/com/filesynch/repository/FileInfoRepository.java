package com.filesynch.repository;

import com.filesynch.entity.ClientInfo;
import com.filesynch.entity.FileInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileInfoRepository extends JpaRepository<FileInfo, Long> {
    FileInfo findByNameAndSizeAndClient(String name, long size, ClientInfo clientInfo);
}
