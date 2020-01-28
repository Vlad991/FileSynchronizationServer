package com.filesynch.converter;

import com.filesynch.dto.FileInfoDTO;
import com.filesynch.entity.FileInfo;

public class FileInfoConverter {
    private ClientInfoConverter clientInfoConverter;

    public FileInfoConverter(ClientInfoConverter clientInfoConverter) {
        this.clientInfoConverter = clientInfoConverter;
    }

    public FileInfoDTO convertToDto(FileInfo fileInfo) {
        FileInfoDTO fileInfoDTO = new FileInfoDTO();
        fileInfoDTO.setName(fileInfo.getName());
        fileInfoDTO.setSize(fileInfo.getSize());
        fileInfoDTO.setClient(clientInfoConverter.convertToDto(fileInfo.getClient()));
        return fileInfoDTO;
    }

    public FileInfo convertToEntity(FileInfoDTO fileInfoDTO) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setName(fileInfoDTO.getName());
        fileInfo.setSize(fileInfoDTO.getSize());
        fileInfo.setClient(clientInfoConverter.convertToEntity(fileInfoDTO.getClient()));
        return fileInfo;
    }
}
