package com.cju.cju_stt;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {
	
	@Autowired
	private ServletContext servletContext;

	public String convertAudioToText(MultipartFile audioFile) {
		System.out.println("[FileService] convertAudioToText()");
		
		String resultText;
		String orgFileName = audioFile.getOriginalFilename();
		String realPath = servletContext.getRealPath("/resources/download/");
		String filePath = realPath+File.separator;
		String saveFileName = System.currentTimeMillis()+orgFileName;		
		String PYTHON_PATH = servletContext.getRealPath("/resources/python/main.py");

       try {
    	   	  audioFile.transferTo(new File(filePath+saveFileName));
	         System.out.println("[FileService] (Save File SUCCESS) => " + filePath+saveFileName);
	      }catch(Exception e) {
	    	  System.out.println("[FileService] (Save File FAIL!) => " + saveFileName);
	         e.printStackTrace();
	      }
        
		ProcessBuilder processBuilder = new ProcessBuilder("python3", PYTHON_PATH , saveFileName, filePath);
		Process process;
		
		try {
			process = processBuilder.start();
			System.out.println("[FileService] (Start process) => " + saveFileName);
			
			try {
			    int exitval = process.waitFor();
			    // InputStream inputStream = process.getInputStream();
			    BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream(),"UTF-8"));
			    
			    // Result data
			    while ((resultText = br.readLine()) != null) {
			    	System.out.println("[FileService] (SUCCESS) => " + saveFileName);
			    	return resultText;
	     	    }
			    System.out.println("[FileService] (** FAIL **) => " + saveFileName);
			    return "문제가 발생하였습니다.";
			       
			} catch (InterruptedException e) {
				e.printStackTrace();
				return null;
			} 
			
		} catch (IOException e) {
			System.out.println("[FileService] Fail processBuilder start.");
			e.printStackTrace();
			return null;
			
		} finally {
			// Delete upload file
		    File delFile = new File(filePath+saveFileName);
		    
		    if(delFile.exists()) {
		    	delFile.delete();
		    	System.out.println("[FileService] (파일삭제 성공) => " + saveFileName);
		    } else {
		    	System.out.println("[FileService] (파일이 존재하지 않습니다.)");
		    }
		}
	}
}
