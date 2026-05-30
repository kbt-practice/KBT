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

@Service
public class ImageHandler {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String save(MultipartFile file) throws IOException {
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename(); // 파일 이름 설정
        Path imageFilePath = Paths.get(uploadDir, filename); // 파일 경로 설정
        Files.copy(file.getInputStream(), imageFilePath, StandardCopyOption.REPLACE_EXISTING); // 실제 파일 저장
        return "/profiles/" + filename; // 클라이언트에서 서버 주소 + 반환값으로 연동
    }
}