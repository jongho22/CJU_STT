package com.cju.cju_stt;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;

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
		
		if (audioFile.isEmpty()) {
			return "변환하실 음성파일을 선택해주세요.";
			
		} else {
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
	
	public String convertAudioToText_API(MultipartFile audioFile) {
		System.out.println("[FileService] convertAudioToText_API");
		
		String openApiURL = "http://aiopen.etri.re.kr:8000/WiseASR/Recognition";
		String accessKey = "b1d4eb07-44f8-42cc-899b-bc9ab42d76fe";    // 발급받은 API Key
		String languageCode = "korean";     // 언어 코드
		
		String resultText;
		String orgFileName = audioFile.getOriginalFilename();
		String realPath = servletContext.getRealPath("/resources/download/");
		String filePath = realPath+File.separator;
		String saveFileName = System.currentTimeMillis()+orgFileName;
		String audioFilePath = filePath+saveFileName;
		String audioContents = null;
		Gson gson = new Gson();
		
		if (audioFile.isEmpty()) {
			return "변환하실 음성파일을 선택해주세요.";
		} else {
			Map<String, Object> request = new HashMap<>();
			Map<String, String> argument = new HashMap<>();
			
			try {
	    	   	  audioFile.transferTo(new File(audioFilePath));
		         System.out.println("[FileService] (Save File SUCCESS) => " + filePath+saveFileName);
		      }catch(Exception e) {
		    	  System.out.println("[FileService] (Save File FAIL!) => " + saveFileName);
		         e.printStackTrace();
		         return "문제가 발생하였습니다.";
		      }
			
			try {
	            Path path = Paths.get(audioFilePath);
	            byte[] audioBytes = Files.readAllBytes(path);
	            audioContents = Base64.getEncoder().encodeToString(audioBytes);
	            System.out.println("[FileService] 인코딩 성공" + audioContents);
	        } catch (IOException e) {
	            e.printStackTrace();
	            return "문제가 발생하였습니다.";
	        }
			argument.put("language_code", languageCode);
	       argument.put("audio", audioContents);
	       request.put("argument", argument);
	       
	       URL url;
	       Integer responseCode = null;
	       String responBody = null;
	       
	       try {
	            url = new URL(openApiURL);
	            HttpURLConnection con = (HttpURLConnection)url.openConnection();
	            con.setRequestMethod("POST");
	            con.setDoOutput(true);
	            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
	            con.setRequestProperty("Authorization", accessKey);
	            
	            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
	            wr.write(gson.toJson(request).getBytes("UTF-8"));
	            wr.flush();
	            wr.close();
	            
	            responseCode = con.getResponseCode();
	            InputStream is = con.getInputStream();
	            byte[] buffer = new byte[is.available()];
	            int byteRead = is.read(buffer);
	            responBody = new String(buffer);
	 
	            System.out.println("[responseCode] " + responseCode);
	            System.out.println("[responBody]");
	            System.out.println(responBody);
	            return responBody;
	 
	        } catch (MalformedURLException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        } finally {
				// Delete upload file
			    File delFile = new File(audioFilePath);
			    
			    if(delFile.exists()) {
			    	delFile.delete();
			    	System.out.println("[FileService] (파일삭제 성공) => " + saveFileName);
			    } else {
			    	System.out.println("[FileService] (파일이 존재하지 않습니다.)");
			    }
			}
	       
		}
	    return null;
	}
}
