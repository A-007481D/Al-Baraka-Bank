package com.albaraka_bank.modules.operation.service.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StoragePort {
    String store(MultipartFile file, Long operationId);
    void delete(String path);
}

