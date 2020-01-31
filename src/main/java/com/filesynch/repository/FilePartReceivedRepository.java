package com.filesynch.repository;

import com.filesynch.entity.FilePartReceived;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FilePartReceivedRepository extends JpaRepository<FilePartReceived, Long> {
}
