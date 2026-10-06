package com.socialauction.backend.global;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

// 업로드 파일 저장·다운로드·삭제 공통 서비스 (상품 이미지, 기관 로고·협약서)
// 저장 파일명: UUID_원본파일명 (원본의 _ 는 - 로 바꿔 구분자로만 쓴다)
@Service
public class FileService {

    // 샘플 데이터 파일(sample.sql)은 git으로 관리하므로 교체·삭제 시에도 지우지 않는다.
    private static final String SAMPLE_PREFIX = "00000000-0000-";

    // 파일 업로드 → 저장된 파일명. 파일이 없으면 null, 저장 실패 시 500 예외.
    public String upload(UploadFolder folder, MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            return null;
        }
        File dir = folder.dir().toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileName = UUID.randomUUID().toString() + "_"
                + multipartFile.getOriginalFilename().replaceAll("_", "-");
        try {
            multipartFile.transferTo(new File(dir, fileName));
            return fileName;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 저장에 실패했습니다.", e);
        }
    }

    // 첨부파일 다운로드 (원본 파일명으로 내려준다)
    public void download(UploadFolder folder, String fileName, HttpServletResponse response) {
        File file = new File(folder.dir().toFile(), fileName);
        if (!file.exists()) {
            return;
        }
        try {
            String realFileName = fileName.substring(fileName.indexOf('_') + 1);
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode(realFileName, StandardCharsets.UTF_8));
            response.setContentLengthLong(file.length());

            try (FileInputStream fin = new FileInputStream(file)) {
                ServletOutputStream fout = response.getOutputStream();
                fin.transferTo(fout);
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "다운로드에 실패했습니다.", e);
        }
    }

    // 파일 삭제. 샘플 파일과 기존 경로("/..." 형태)는 건드리지 않는다.
    public void delete(UploadFolder folder, String fileName) {
        if (fileName == null || fileName.isBlank()) return;
        if (fileName.startsWith(SAMPLE_PREFIX) || fileName.startsWith("/")) return;
        File file = new File(folder.dir().toFile(), fileName);
        if (file.exists()) {
            file.delete();
        }
    }
}
