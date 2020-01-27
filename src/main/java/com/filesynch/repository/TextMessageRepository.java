package com.filesynch.repository;

import com.filesynch.dto.TextMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Repository
public interface TextMessageRepository extends JpaRepository<TextMessage, Long> {
}
