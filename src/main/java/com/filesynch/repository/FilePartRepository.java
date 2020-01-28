package com.filesynch.repository;

import com.filesynch.entity.FilePart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FilePartRepository extends JpaRepository<FilePart, Long> {
}
