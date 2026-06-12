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

/**
 * 이미지 파일 저장/삭제를 담당하는 핸들러.
 *
 * 저장 시 3단계 보안 검증을 순서대로 수행한다.
 *   1. 파일 확장자 검증 (화이트리스트 방식)
 *   2. Content-Type 헤더 검증 (MIME 타입)
 *   3. Magic Bytes 검증 (실제 파일 내용)
 *
 * Content-Type은 클라이언트가 임의로 위조할 수 있으므로 Magic Bytes 검증이 최종 보루가 된다.
 */
@Slf4j
@Service
public class ImageHandler {

    /** 허용하는 파일 확장자 목록 (소문자 기준) */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    /** 허용하는 MIME 타입 목록 */
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpg", "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.profile-access-path}")
    private String profileAccessPath;

    @Value("${file.posts-access-path}")
    private String postsAccessPath;

    /**
     * 프로필 이미지를 저장하고 접근 URL을 반환한다.
     *
     * @param file 업로드된 MultipartFile
     * @return 저장된 파일의 접근 경로 (예: /profiles/uuid_filename.jpg)
     * @throws CustomException 디렉토리 미존재, 유효성 검증 실패, IO 오류 시
     */
    public String profileSave(MultipartFile file) {
        checkUploadDir();
        validateImage(file);
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path imageFilePath = Paths.get(uploadDir, filename);
        try {
            Files.copy(file.getInputStream(), imageFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("프로필 이미지 저장 실패: {}", filename, e);
            throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
        return profileAccessPath + "/" + filename;
    }

    /**
     * 게시글 이미지를 저장하고 접근 URL을 반환한다.
     *
     * @param file 업로드된 MultipartFile
     * @return 저장된 파일의 접근 경로 (예: /profiles/uuid_filename.jpg)
     * @throws CustomException 디렉토리 미존재, 유효성 검증 실패, IO 오류 시
     */
    public String postSave(MultipartFile file) {
        checkUploadDir();
        validateImage(file);
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path imageFilePath = Paths.get(uploadDir, filename);
        try {
            Files.copy(file.getInputStream(), imageFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("게시글 이미지 저장 실패: {}", filename, e);
            throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
        return postsAccessPath + "/" + filename;
    }

    /**
     * 이미지 URL에 해당하는 파일을 디스크에서 삭제한다.
     * URL이 null이거나 빈 값이면 아무 작업도 하지 않는다.
     * 삭제 실패 시 예외를 던지지 않고 경고 로그만 남긴다.
     * 트랜잭션 콜백 등 실패해도 롤백할 수 없는 컨텍스트에서 사용한다.
     *
     * @param imageUrl 삭제할 이미지의 접근 경로 (예: /profiles/uuid_filename.jpg)
     */
    public void deleteSafely(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return;
        String filename = Paths.get(imageUrl).getFileName().toString();
        Path filePath = Paths.get(uploadDir, filename);
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("이미지 삭제 실패 (고아 파일 가능성): {}", filePath, e);
        }
    }

    /**
     * 업로드 디렉토리가 실제로 존재하는지 확인한다.
     * 서버 설정 오류나 마운트 해제 등의 상황을 저장 전에 조기 감지하기 위해 매 저장마다 호출한다.
     */
    private void checkUploadDir() {
        Path dirPath = Paths.get(uploadDir);
        if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
            throw new CustomException(ErrorCode.UPLOAD_DIR_NOT_FOUND);
        }
    }

    /**
     * 확장자 → Content-Type → Magic Bytes 순서로 이미지 유효성을 검증한다.
     * 앞 단계에서 실패하면 이후 단계는 실행되지 않는다.
     */
    private void validateImage(MultipartFile file) {
        validateExtension(file.getOriginalFilename());
        validateContentType(file.getContentType());
        try {
            validateMagicBytes(file);
        } catch (IOException e) {
            log.error("Magic Bytes 검증 중 IO 오류", e);
            throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }

    /**
     * 파일명에서 확장자를 추출하여 허용 목록에 있는지 확인한다.
     * 확장자가 없는 파일명(.hidden 등)도 차단한다.
     * 대소문자 구분 없이 검사한다 (toLowerCase 적용).
     */
    private void validateExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new CustomException(ErrorCode.INVALID_IMAGE_EXTENSION);
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new CustomException(ErrorCode.INVALID_IMAGE_EXTENSION);
        }
    }

    /**
     * HTTP 요청의 Content-Type 헤더 값이 허용 MIME 타입인지 확인한다.
     * 이 값은 클라이언트가 위조할 수 있으므로, 반드시 validateMagicBytes()와 함께 사용해야 한다.
     */
    private void validateContentType(String contentType) {
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new CustomException(ErrorCode.INVALID_IMAGE_MIME_TYPE);
        }
    }

    /**
     * 파일의 첫 번째 바이트(Magic Bytes)를 읽어 실제 이미지 포맷인지 검증한다.
     * 확장자나 Content-Type을 위조하더라도 파일 내용 자체로 판별하므로 가장 신뢰도가 높은 검증이다.
     *
     * 각 포맷의 Magic Bytes:
     *   JPEG : FF D8 FF
     *   PNG  : 89 50 4E 47 (‰PNG)
     *   GIF  : 47 49 46 38 (GIF8)
     *   WebP : 52 49 46 46 ?? ?? ?? ?? 57 45 42 50 (RIFF....WEBP)
     */
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

    /** JPEG: 시작 3바이트가 FF D8 FF */
    private boolean isJpeg(byte[] h) {
        return h[0] == (byte) 0xFF && h[1] == (byte) 0xD8 && h[2] == (byte) 0xFF;
    }

    /** PNG: 시작 4바이트가 89 50 4E 47 (‰PNG) */
    private boolean isPng(byte[] h) {
        return h[0] == (byte) 0x89 && h[1] == 0x50 && h[2] == 0x4E && h[3] == 0x47;
    }

    /** GIF: 시작 4바이트가 47 49 46 38 (GIF8) */
    private boolean isGif(byte[] h) {
        return h[0] == 0x47 && h[1] == 0x49 && h[2] == 0x46 && h[3] == 0x38;
    }

    /**
     * WebP: RIFF 컨테이너 포맷으로 1~4번째 바이트가 RIFF, 9~12번째 바이트가 WEBP.
     * 5~8번째 4바이트는 파일 크기 값이므로 검사에서 제외한다.
     */
    private boolean isWebp(byte[] h, int read) {
        return read >= 12
                && h[0] == 0x52 && h[1] == 0x49 && h[2] == 0x46 && h[3] == 0x46  // RIFF
                && h[8] == 0x57 && h[9] == 0x45 && h[10] == 0x42 && h[11] == 0x50; // WEBP
    }
}
