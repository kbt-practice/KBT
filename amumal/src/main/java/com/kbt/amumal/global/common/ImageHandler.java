package com.kbt.amumal.global.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ImageHandler {

    @Value("${file.upload-dir}")
    private String uploadDir;
    public String profileSave(MultipartFile file) throws IOException {
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename(); // 파일 이름 설정
        Path imageFilePath = Paths.get(uploadDir, filename); // 파일 경로 설정
        Files.copy(file.getInputStream(), imageFilePath, StandardCopyOption.REPLACE_EXISTING); // 실제 파일 저장
        return "/profiles/" + filename; // 클라이언트에서 서버 주소 + 반환값으로 연동
    }

    public String postSave(MultipartFile file) throws IOException {
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename(); // 파일 이름 설정
        Path imageFilePath = Paths.get(uploadDir, filename); // 파일 경로 설정
        Files.copy(file.getInputStream(), imageFilePath, StandardCopyOption.REPLACE_EXISTING); // 실제 파일 저장
        return "/profiles/" + filename; // 클라이언트에서 서버 주소 + 반환값으로 연동
    }

    public void delete(String imageUrl) throws IOException {
        if (imageUrl == null || imageUrl.isBlank()) return;
        String filename = Paths.get(imageUrl).getFileName().toString();
        Path filePath = Paths.get(uploadDir, filename);
        Files.deleteIfExists(filePath);
    }
}