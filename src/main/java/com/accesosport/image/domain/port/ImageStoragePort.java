package com.accesosport.image.domain.port;

public interface ImageStoragePort {

    UploadResult upload(byte[] bytes, String contentType, String folder, Object publicIdSeed);

    void delete(String publicId);
}
