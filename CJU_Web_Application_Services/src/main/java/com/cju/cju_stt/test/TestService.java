package com.cju.cju_stt.test;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletContext;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

import com.google.gson.Gson;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

@Service
public class TestService {
	
	@Autowired
	private ServletContext servletContext;
	
	public SseEmitter streamSseMvc() {
		SseEmitter emitter = new SseEmitter();

		ExecutorService sseMvcExecutor = Executors.newSingleThreadExecutor();
		sseMvcExecutor.execute(() -> {
			try {
				String result ;
				for (int i = 0; i < 20; i++) {
				result = "check" + i;
				SseEventBuilder event = SseEmitter.event().data(result);
				emitter.send(event);
				Thread.sleep(1000);
				}
			} catch (Exception ex) {
				emitter.completeWithError(ex);
			}
		});
		return emitter;
	}
	
	// API를 사용하는 STT
	public SseEmitter convertAudioToText_API(MultipartFile audioFile) {
		System.out.println("[FileService] convertAudioToText_API");

		// API 설정
		String openApiURL   = "http://aiopen.etri.re.kr:8000/WiseASR/Recognition";
		String accessKey    = "b1d4eb07-44f8-42cc-899b-bc9ab42d76fe";
		String languageCode = "korean";

		// 파일 설정
		String orgFileName 	  = audioFile.getOriginalFilename();
		String baseName 		  = System.currentTimeMillis() + FilenameUtils.getBaseName(orgFileName);
		String realPath 		  = servletContext.getRealPath("/resources/download/");
		String filePath 		  = realPath + File.separator;
		String saveFileName 	  = System.currentTimeMillis() + orgFileName;
		String savePcmFilePath = filePath + baseName + ".pcm";
		String audioFilePath   = filePath + saveFileName;
		String audioContents   = null;
		Gson gson = new Gson();
		
		SseEmitter emitter = new SseEmitter();

		if (audioFile.isEmpty()) { // Form이 비어있을 경우
			return null;
		} else {
			Map<String, Object> request = new HashMap<>();
			Map<String, String> argument = new HashMap<>();

			try {
				audioFile.transferTo(new File(audioFilePath));
				try {
					// 음성파일 길이 확인
					String ffprobeCommand = "ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 "
							+ audioFilePath;
					ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", ffprobeCommand);
					Process process = processBuilder.start();
					BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
					String output = reader.readLine();
					
					if (output != null) {	// 음성 파일 길이가 산출되었을 경우
						double durationInSeconds = Double.parseDouble(output);
						System.out.println("[FileService] 음성파일 길이 : " + durationInSeconds);
						
						// 20초가 넘어가는 음성파일
						if (durationInSeconds > 20) {
							System.out.println("[FileService] 20초 이상의 파일입니다. 분할을 시작합니다.");
							String outputFormat = baseName  + "%d.mp3";
							String ffmpegCommand = String.format("ffmpeg -i %s -f segment -segment_time 18 -c copy %s",
									audioFilePath, filePath + outputFormat);
							try {
								ProcessBuilder processBuilder2 = new ProcessBuilder("bash", "-c", ffmpegCommand);
								Process process2 = processBuilder2.start();
								process2.waitFor();

								int countFile = (int) Math.ceil(durationInSeconds / 18);
								
								FFmpeg ffmpeg = new FFmpeg("/usr/bin/ffmpeg");
								
								ExecutorService sseMvcExecutor = Executors.newSingleThreadExecutor();
								sseMvcExecutor.execute(() -> {
									try {
										String resultText = null;
										for (int i = 0; i <= countFile; i++) {
										resultText = convertText(savePcmFilePath, audioContents, filePath + baseName + i + ".mp3" ,
												argument, languageCode, request, openApiURL, accessKey, gson, ffmpeg);
												System.out.println(resultText);
										SseEventBuilder event = SseEmitter.event().data(resultText);
										emitter.send(event);
										Thread.sleep(1000);
										}
									} catch (Exception ex) {
										emitter.completeWithError(ex);
									}
								});
								
								return emitter;
								
							} catch (IOException | InterruptedException e) {
								e.printStackTrace();
								return null;
							}
						} else { // 20초가 넘어가지 않는 음성파일
							System.out.println("[FileService] 20초 미만의 파일입니다.");
							
							// API 요청 실행
							try {
								FFmpeg ffmpeg = new FFmpeg("/usr/bin/ffmpeg");
								String resultText = convertText(savePcmFilePath, audioContents, audioFilePath,
								argument, languageCode, request, openApiURL, accessKey, gson, ffmpeg);
								return null;
								
							} catch (IOException e) {
								e.printStackTrace();
								delFile(audioFilePath);
								delFile(savePcmFilePath);
								return null;
							} 
						}	
					} else {
						System.out.println("[FileService] 음성 길이 계산 실패");
						delFile(audioFilePath);
						return null;
					}
				} catch (Exception e) {
					e.printStackTrace();
					delFile(audioFilePath);
					return null;
				}
			} catch (Exception e) {
				System.out.println("[FileService] (Save File FAIL!) => " + savePcmFilePath);
				e.printStackTrace();
				return null;
			}
		}
	}
		
	// mp3 파일을 PCM 형식으로 변환
	public void mp3ToPcm(FFmpeg ffmpeg, String audioFilePath, String savePcmFilePath) throws IOException {

		// FFmpeg 실행
		FFmpegBuilder builder = new FFmpegBuilder().setInput(audioFilePath).addOutput(savePcmFilePath)
				.setAudioCodec("pcm_s16le").setFormat("s16le").setAudioChannels(1).setAudioSampleRate(16000).done();
		FFmpegExecutor executor = new FFmpegExecutor(ffmpeg);
		executor.createJob(builder).run();
	}
	
	// 파일 삭제
	public void delFile(String audioFilePath) {
		File delFile = new File(audioFilePath);
		if (delFile.exists()) {
			delFile.delete();
			System.out.println("[FileService] (음성파일 파일삭제 성공)");
		} else {
			System.out.println("[FileService] (음성파일이 존재하지 않습니다.)");
		}
	}
	
	// API서버에 요청
	public String convertText(String savePcmFilePath, String audioContents, String audioFilePath,
			Map<String, String> argument, String languageCode, Map<String, Object> request, String openApiURL,
			String accessKey, Gson gson, FFmpeg ffmpeg) throws MalformedURLException, ProtocolException, IOException{
		
		// mp3파일을 PCM형식으로 변환
		try {
			mp3ToPcm(ffmpeg, audioFilePath, savePcmFilePath);
			System.out.println("[FileService] (Make PCM File SUCCESS)");
		} catch (IOException e) {
			e.printStackTrace();
			return "파일 PCM 변환 과정에서 문제가 발생하였습니다.";
		}
		
		// 음성파일 바이트 변환
		try {
			Path path = Paths.get(savePcmFilePath);
			byte[] audioBytes = Files.readAllBytes(path);
			audioContents = Base64.getEncoder().encodeToString(audioBytes);
			System.out.println("[FileService] (음성파일 바이트 변환 성공)");
			
		} catch (IOException e) { // 음성파일 바이트 변환에 실패 했을 경우 음성파일 삭제
			e.printStackTrace();
			delFile(audioFilePath);
		}

		// API 요청값 입력
		argument.put("language_code", languageCode);
		argument.put("audio", audioContents);
		request.put("argument", argument);

		URL url;
		Integer responseCode = null;
		String responBody = null;

		// API 요청 및 결과 리턴
		
		url = new URL(openApiURL);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
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

		// 결과 값
		Map<String, Object> responseMap = gson.fromJson(responBody, Map.class);
		Map<String, Object> returnObject = (Map<String, Object>) responseMap.get("return_object");
		String recognizedText = (String) returnObject.get("recognized");

		System.out.println("[FileService] [responseCode] " + responseCode);
		System.out.println("[FileService] [resultText] " + recognizedText);
		
		delFile(audioFilePath);
		delFile(savePcmFilePath);
	
		return recognizedText;
	}	
}
