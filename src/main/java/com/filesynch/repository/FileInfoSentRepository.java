package com.filesynch.repository;

import com.filesynch.entity.FileInfoSent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileInfoSentRepository extends JpaRepository<FileInfoSent, Long> {
    FileInfoSent findByNameAndSizeAndClient_Login(String name, long size, String login);
}
