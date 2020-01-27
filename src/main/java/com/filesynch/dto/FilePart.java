package com.filesynch.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "file_parts")
public class FilePart {
    @Id
    private Long hashKey;
    @ManyToOne
    @JoinColumn(name = "file_info_id")
    private FileInfo fileInfo;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private FilePartStatus status;
    @Column(name = "is_first")
    private boolean isFirst;
    private byte[] data;
    private int length;
    @ManyToOne
    @JoinColumn(name = "client_id")
    private ClientInfo client;
}
