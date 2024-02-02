package com.cju.cju_stt;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

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
		
		// API 설정
		String openApiURL = "http://aiopen.etri.re.kr:8000/WiseASR/Recognition";
		String accessKey = "b1d4eb07-44f8-42cc-899b-bc9ab42d76fe";   
		String languageCode = "korean";     
		
		// 파일 설정 
		String orgFileName = audioFile.getOriginalFilename();
		String realPath = servletContext.getRealPath("/resources/download/");
		String filePath = realPath+File.separator;
		String saveFileName = System.currentTimeMillis()+orgFileName;
		String savePcmFilePath = filePath + System.currentTimeMillis()+"output.pcm";
		String audioFilePath = filePath+saveFileName;
		String audioContents = null;
		Gson gson = new Gson();
		
		if (audioFile.isEmpty()) {
			return "변환하실 음성파일을 선택해주세요.";
		} else {
			Map<String, Object> request = new HashMap<>();
			Map<String, String> argument = new HashMap<>();
			
			// PCM 형식 변환
			try {
	    	   	  audioFile.transferTo(new File(audioFilePath));
	    	   	  try {
	    	   	  		String ffprobeCommand = "ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 " + audioFilePath;
	    	   	  		ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", ffprobeCommand);
	    	   	  		Process process = processBuilder.start();
	    	   	  		
	    	   	  		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	    	   	  		String output = reader.readLine();
	    	   	  		if (output != null) {
			                double durationInSeconds = Double.parseDouble(output);
			                System.out.println(durationInSeconds);
			                if(durationInSeconds>20) {
	    	   	  					System.out.println("20초 이상");
	    	   	  					String outputFormat = orgFileName+"_%03d.mp3";
	    	   	  					String ffmpegCommand = String.format("ffmpeg -i %s -f segment -segment_time 19 -c copy %s", audioFilePath, audioFilePath+outputFormat);
					    	   		try {
					    	   			 System.out.println(ffmpegCommand);
							            ProcessBuilder processBuilder2 = new ProcessBuilder("bash", "-c", ffmpegCommand);
							            Process process2 = processBuilder2.start();
							            process2.waitFor();
							            
							            int countFile= (int) Math.ceil(durationInSeconds / 19);
							            FFmpeg ffmpeg = new FFmpeg("/usr/bin/ffmpeg");
							            
							            for(int i=0;i<=countFile;i++){
							            	
//							            	mp3ToPcm(ffmpeg, audioFilePath, filePath+orgFileName+"_"+i+".pcm");
//							            	System.out.println(filePath+orgFileName+"_"+i+".pcm");
//							            	convertText(filePath+orgFileName+"_"+i+".pcm", audioContents, audioFilePath, saveFileName, argument, languageCode, request, openApiURL, accessKey, gson);
							         
							             }
							 			  
							            System.out.println("Audio splitting completed.");
							        } catch (IOException | InterruptedException e) {
							            e.printStackTrace();
							        }
					    	   } 
			                  
			            } else {
			                System.out.println("[FileService] 음성 길이 계산 실패");
			            }
			            
			            
	    	   	  } catch (Exception e) {
            			e.printStackTrace();
	    	   	  }
	    	   	  
	    	   	  FFmpeg ffmpeg = new FFmpeg("/usr/bin/ffmpeg");
	    	   	  
		    	   	try {
		                // FFmpeg 실행
		                FFmpegBuilder builder = new FFmpegBuilder()
		                        .setInput(audioFilePath) 
		                        .addOutput(savePcmFilePath) 
		                        .setAudioCodec("pcm_s16le")
		                        .setFormat("s16le")
		                        .setAudioChannels(1)
		                        .setAudioSampleRate(16000)
		                        .done();
           
		                FFmpegExecutor executor = new FFmpegExecutor(ffmpeg);
		                executor.createJob(builder).run();
		            } catch (IOException e) {
		                e.printStackTrace();
		                return "파일 PCM 변환 과정에서 문제가 발생하였습니다.";
		            }
		         System.out.println("[FileService] (Make PCM File SUCCESS) => " + savePcmFilePath);
		         
		      }catch(Exception e) {
		    	  System.out.println("[FileService] (Save File FAIL!) => " + savePcmFilePath);
		         e.printStackTrace();
		         return "파일 저장 과정에서 문제가 발생하였습니다.";
		      }
			
			// 음성파일 바이트 변환
			try {
	            Path path = Paths.get(savePcmFilePath);
	            byte[] audioBytes = Files.readAllBytes(path);
	            audioContents = Base64.getEncoder().encodeToString(audioBytes);
	            
	        } catch (IOException e) {
	            e.printStackTrace();
	            File delFile = new File(audioFilePath);
	            if(delFile.exists()) {
				    	delFile.delete();
				    	System.out.println("[FileService] (파일삭제 성공) => " + saveFileName);
				    } else {
				    	System.out.println("[FileService] (파일이 존재하지 않습니다.)");
				    }
	            return "인코딩 과정에서 문제가 발생하였습니다.";
	        }
	        
	        // API 요청값 입력
			argument.put("language_code", languageCode);
	       argument.put("audio", audioContents);
	       request.put("argument", argument);
	       
	       URL url;
	       Integer responseCode = null;
	       String responBody = null;
	       
	       // API 요청 및 결과 리턴
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
	            
	            // Json 형식 
	            Map<String, Object> responseMap = gson.fromJson(responBody, Map.class);
	            Map<String, Object> returnObject = (Map<String, Object>) responseMap.get("return_object");
	            String recognizedText = (String) returnObject.get("recognized");
	            
	            System.out.println("[FileService] [responseCode] " + responseCode);
	            System.out.println("[FileService] [resultText] " + recognizedText);
	    
	            return recognizedText;
	 
	        } catch (MalformedURLException e) {
	            e.printStackTrace();
	            return "API 서버 연결에 문제가 있습니다.";
	            
	        } catch (IOException e) {
	            e.printStackTrace();
	            return "파일의 길이가 20초를 넘겼습니다.";
	            
	        } finally {
			    File delFile = new File(audioFilePath);
			    File delPcmFile = new File(savePcmFilePath);
			    
			    if(delFile.exists()) {
			    	delFile.delete();
			    	System.out.println("[FileService] (파일삭제 성공) => " + saveFileName);
			    } else {
			    	System.out.println("[FileService] (파일이 존재하지 않습니다.)");
			    }
			    
			    if(delPcmFile.exists()) {
			    	delPcmFile.delete();
			    	System.out.println("[FileService] (PCM 파일삭제 성공)");
			    } else {
			    	System.out.println("[FileService] (파일이 존재하지 않습니다.)");
			    }
			}
		}
	}
	
	public void mp3ToPcm (FFmpeg ffmpeg, String audioFilePath, String savePcmFilePath) {
	    	   	  
	   	try {
            // FFmpeg 실행
            FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(audioFilePath) 
                    .addOutput(savePcmFilePath) 
                    .setAudioCodec("pcm_s16le")
                    .setFormat("s16le")
                        .setAudioChannels(1)
                        .setAudioSampleRate(16000)
                        .done();
   
                FFmpegExecutor executor = new FFmpegExecutor(ffmpeg);
                executor.createJob(builder).run();
       } catch (IOException e) {
                e.printStackTrace();
       	}
 		System.out.println("[FileService] (Make PCM File SUCCESS) => " + savePcmFilePath);
	}
	
	public void convertText(String savePcmFilePath, String audioContents, String audioFilePath, String saveFileName, Map<String, String> argument, String languageCode, Map<String, Object> request, String openApiURL, String accessKey, Gson gson) {
		// 음성파일 바이트 변환
			try {
				 System.out.println(savePcmFilePath);
	            Path path = Paths.get(savePcmFilePath);
	            byte[] audioBytes = Files.readAllBytes(path);
	            audioContents = Base64.getEncoder().encodeToString(audioBytes);
	            
	        } catch (IOException e) {
	            e.printStackTrace();
	            File delFile = new File(audioFilePath);
	            if(delFile.exists()) {
				    	delFile.delete();
				    	System.out.println("[FileService] (파일삭제 성공) => " + saveFileName);
				    } else {
				    	System.out.println("[FileService] (파일이 존재하지 않습니다.)");
				    }
	           
	        }
	        
	        // API 요청값 입력
			argument.put("language_code", languageCode);
	       argument.put("audio", audioContents);
	       request.put("argument", argument);
	       
	       URL url;
	       Integer responseCode = null;
	       String responBody = null;
	       
	       // API 요청 및 결과 리턴
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
	            
	            // Json 형식 
	            Map<String, Object> responseMap = gson.fromJson(responBody, Map.class);
	            Map<String, Object> returnObject = (Map<String, Object>) responseMap.get("return_object");
	            String recognizedText = (String) returnObject.get("recognized");
	            
	            System.out.println("[FileService] [responseCode] " + responseCode);
	            System.out.println("[FileService] [resultText] " + recognizedText);
	    
	           
	 
	        } catch (MalformedURLException e) {
	           
	            
	            
	        } catch (IOException e) {
	       
	            
            
	        }
	}
	
}
