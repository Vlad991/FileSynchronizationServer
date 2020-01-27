package com.filesynch.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "file_info")
public class FileInfo {
    @Id
    @GeneratedValue(generator="increment")
    @GenericGenerator(name="increment", strategy = "increment")
    private Long id;
    @Column(name = "name")
    private String name;
    @Column(name = "size")
    private long size; // in bytes
    @ManyToOne
    @JoinColumn(name = "client_id")
    private ClientInfo client;
}
