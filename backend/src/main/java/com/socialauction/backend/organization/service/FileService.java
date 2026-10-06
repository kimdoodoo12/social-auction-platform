package com.socialauction.backend.organization.service;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLEncoder;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

@Service 
public class FileService {
    
    // 프로젝트 최상위 경로 찾기
    String baseDir = System.getProperty("user.dir");

    // 서버 build용 경로
    private String uploadPath = baseDir+"/backend/build/resources/main/static/organization/";

    public String fileUpload(MultipartFile multipartFile){
        if (multipartFile == null || multipartFile.isEmpty()){return null;}

        // 실제 uploadpath에 그 경로에 파일이 있는지 확인
        File dir = new File(uploadPath);
        if (!dir.exists()){dir.mkdir();}

        String fileName = UUID.randomUUID().toString()+"_"+multipartFile.getOriginalFilename().replaceAll("_", "-");
    
        // 파일 업로드
        try{
            multipartFile.transferTo(new File(uploadPath+fileName));
            return fileName;            
        }catch(Exception e){System.out.println(e);}
        return null;
    }

    public void fileDownload(String fileName, HttpServletResponse response){

        String downloadPath = uploadPath + fileName;

        // 파일경로 있는지 확인
        File file = new File(downloadPath);
        if (!file.exists()){return;}

        try{
            String realFileName = fileName.split("_")[1];
            long fileSize = file.length();
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename="+URLEncoder.encode(realFileName, "UTF-8"));
            response.setContentLengthLong(fileSize);

            FileInputStream fin = new FileInputStream(file);
            ServletOutputStream fout = response.getOutputStream();
            try{
                fin.transferTo(fout);
            }finally{fin.close();}
            
        }catch(Exception e){System.out.println(e);}
    }

    public void fileDelete(String fileName) {
        if (fileName == null || fileName.isBlank()) { return; }
        File file = new File(uploadPath + fileName);
        if (file.exists()) { file.delete(); }
    }
}
