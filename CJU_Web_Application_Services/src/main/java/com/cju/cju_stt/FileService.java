package com.cju.cju_stt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

	public String convertAudioToText(MultipartFile audioFile) {
		System.out.println("[FileService] convertAudioToText()");
		
		String fileName = audioFile.getOriginalFilename();
		String line;
		
		ProcessBuilder processBuilder = new ProcessBuilder("python3", "/home/jongho/바탕화면/test2.py", fileName);
		Process process;
		
		try {
			process = processBuilder.start();
			
			try {
			    int exitval = process.waitFor();
			    InputStream inputStream = process.getInputStream();
			    BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream(),"UTF-8"));

			    
			    while ((line = br.readLine()) != null) {
			    	return line;
			       //System.out.println(line);
	     	    }
		        
			    if(exitval !=0){
			        //비정상종료
			       System.out.println("문제가 발생하였습니다.");
			       return "문제가 발생하였습니다.";
			     }
			    
			    String result  = fileName + "결과 입니다. \n" + line;
			    System.out.println(result);
			    
			    return result;
			} catch (InterruptedException e) {
				e.printStackTrace();
				return null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		// String result = audioFile.getOriginalFilename();
	
	}
}
