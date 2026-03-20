package com.accesosport.image.infrastructure.cloudinary;

import com.accesosport.image.domain.exception.ImageUploadException;
import com.accesosport.image.domain.port.ImageStoragePort;
import com.accesosport.image.domain.port.UploadResult;
import com.accesosport.shared.domain.i18n.MessageKeys;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class CloudinaryImageStorageAdapter implements ImageStoragePort {

    private final Cloudinary cloudinary;

    @Override
    public UploadResult upload(byte[] bytes, String contentType, String folder, Object publicIdSeed) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(bytes, ObjectUtils.asMap(
                    "folder", folder,
                    "public_id", publicIdSeed.toString(),
                    "resource_type", "image",
                    "overwrite", true
            ));
            String url = (String) result.get("secure_url");
            String publicId = (String) result.get("public_id");
            return new UploadResult(url, publicId);
        } catch (IOException e) {
            log.error("Failed to upload image to Cloudinary", e);
            throw new ImageUploadException(MessageKeys.Images.UPLOAD_FAILED, e);
        }
    }

    @Override
    public void delete(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            log.error("Failed to delete image from Cloudinary: {}", publicId, e);
            throw new ImageUploadException(MessageKeys.Images.UPLOAD_FAILED, e);
        }
    }
}
