package com.socialauction.backend.products.admin.service;

import java.io.File;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service 
public class FileService {
    private String baseDir = System.getProperty("user.dir");
    // 경로 지정
    private String uploadPath = baseDir+"/src/main/resources/static/images/";

    // 파일 업로드
    public String fildUpload( MultipartFile multipartFile ){

        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "업로드할 파일을 선택해주세요.");
        }
        File dir = new File( uploadPath ); 
        if( !dir.exists() ){ dir.mkdir(); }
        String fileName = UUID.randomUUID().toString()+"_"+multipartFile.getOriginalFilename()
                                                                        .replaceAll("_", "-");
        try{
            multipartFile.transferTo( new File( uploadPath+fileName ) );
            return fileName;
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "이미지 파일 저장에 실패했습니다.", e);
        }
    }   
}
