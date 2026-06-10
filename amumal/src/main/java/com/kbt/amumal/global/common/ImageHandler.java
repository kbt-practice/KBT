package com.kbt.amumal.global.common;

import com.kbt.amumal.global.error.CustomException;
import com.kbt.amumal.global.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class ImageHandler {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpg","image/jpeg", "image/png", "image/gif", "image/webp"
    );

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String profileSave(MultipartFile file) throws IOException {
        checkUploadDir();
        validateImage(file);
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path imageFilePath = Paths.get(uploadDir, filename);
        Files.copy(file.getInputStream(), imageFilePath, StandardCopyOption.REPLACE_EXISTING);
        return "/profiles/" + filename;
    }

    public String postSave(MultipartFile file) throws IOException {
        checkUploadDir();
        validateImage(file);
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path imageFilePath = Paths.get(uploadDir, filename);
        Files.copy(file.getInputStream(), imageFilePath, StandardCopyOption.REPLACE_EXISTING);
        return "/profiles/" + filename;
    }

    public void delete(String imageUrl) throws IOException {
        if (imageUrl == null || imageUrl.isBlank()) return;
        String filename = Paths.get(imageUrl).getFileName().toString();
        Path filePath = Paths.get(uploadDir, filename);
        Files.deleteIfExists(filePath);
    }

    private void checkUploadDir() {
        Path dirPath = Paths.get(uploadDir);
        if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
            throw new CustomException(ErrorCode.UPLOAD_DIR_NOT_FOUND);
        }
    }

    private void validateImage(MultipartFile file) throws IOException {
        validateExtension(file.getOriginalFilename());
        validateContentType(file.getContentType());
        validateMagicBytes(file);
    }

    private void validateExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new CustomException(ErrorCode.INVALID_IMAGE_EXTENSION);
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new CustomException(ErrorCode.INVALID_IMAGE_EXTENSION);
        }
    }

    private void validateContentType(String contentType) {
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new CustomException(ErrorCode.INVALID_IMAGE_MIME_TYPE);
        }
    }

    // 클라이언트가 Content-Type을 위조할 수 있으므로 실제 파일 헤더(Magic Bytes)로 검증
    private void validateMagicBytes(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[12];
            int read = is.read(header);
            if (read < 4) {
                throw new CustomException(ErrorCode.INVALID_IMAGE_MIME_TYPE);
            }
            if (!isJpeg(header) && !isPng(header) && !isGif(header) && !isWebp(header, read)) {
                throw new CustomException(ErrorCode.INVALID_IMAGE_MIME_TYPE);
            }
        }
    }

    private boolean isJpeg(byte[] h) {
        return h[0] == (byte) 0xFF && h[1] == (byte) 0xD8 && h[2] == (byte) 0xFF;
    }

    private boolean isPng(byte[] h) {
        return h[0] == (byte) 0x89 && h[1] == 0x50 && h[2] == 0x4E && h[3] == 0x47;
    }

    private boolean isGif(byte[] h) {
        return h[0] == 0x47 && h[1] == 0x49 && h[2] == 0x46 && h[3] == 0x38;
    }

    private boolean isWebp(byte[] h, int read) {
        // RIFF....WEBP 구조
        return read >= 12
                && h[0] == 0x52 && h[1] == 0x49 && h[2] == 0x46 && h[3] == 0x46
                && h[8] == 0x57 && h[9] == 0x45 && h[10] == 0x42 && h[11] == 0x50;
    }
}
