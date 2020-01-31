package com.filesynch.repository;

import com.filesynch.entity.FilePartSent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FilePartSentRepository extends JpaRepository<FilePartSent, Long> {
}
