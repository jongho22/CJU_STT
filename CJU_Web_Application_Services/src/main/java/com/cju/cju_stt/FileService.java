package com.cju.cju_stt;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

	public String convertAudioToText(MultipartFile audioFile) {
		System.out.println("[FileService] convertAudioToText()");
		
		String resultText;
		String orgFileName = audioFile.getOriginalFilename();
	    long fileSize = audioFile.getSize();
	    String realPath = "/home/jongho/바탕화면/";
	    String filePath = realPath+File.separator;
	    
	    File upload=new File(filePath);
	      if(!upload.exists()) {  // 디렉토리가 존재하지 않으면 
	         upload.mkdir();      
	      }
       String saveFileName = System.currentTimeMillis()+orgFileName;
       
       try {
    	   	  audioFile.transferTo(new File(filePath+saveFileName));
	         System.out.println("[FileService] (Save File) => "+filePath+saveFileName);
	         
	      }catch(Exception e) {
	         e.printStackTrace();
	      }
		ProcessBuilder processBuilder = new ProcessBuilder("python3", "/home/jongho/바탕화면/test2.py", orgFileName);
		Process process;
		
		try {
			process = processBuilder.start();
			
			try {
			    int exitval = process.waitFor();
			    InputStream inputStream = process.getInputStream();
			    BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream(),"UTF-8"));
			    
			    // Delete upload file
			    File delFile = new File(filePath+saveFileName);
			    
			    if(delFile.exists()) {
			    	delFile.delete();
			    	System.out.println("[FileService] 파일삭제 성공");
			    } else {
			    	System.out.println("[FileService] 파일이 존재하지 않습니다.");
			    }
			    
			    // Result data
			    while ((resultText = br.readLine()) != null) {
			    	System.out.println("[FileService] SUCCESS!!");
			    	return resultText;
	     	    }
			    System.out.println("[FileService] 문제가 발생하였습니다.");
			    return "문제가 발생하였습니다.";
			       
			} catch (InterruptedException e) {
				e.printStackTrace();
				return null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
