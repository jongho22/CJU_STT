package com.cju.cju_stt.socket;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

import javax.servlet.ServletContext;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import com.google.gson.Gson;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

public class WebSocketHandlerForAPI extends BinaryWebSocketHandler {
	
	protected String fileName;

	protected String openApiURL   = "http://aiopen.etri.re.kr:8000/WiseASR/Recognition";
	protected String accessKey    = "b1d4eb07-44f8-42cc-899b-bc9ab42d76fe";
	protected String languageCode = "korean";
		
	@Override
	protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
		byte[] audioData = message.getPayload().array();
		fileName = System.currentTimeMillis()+fileName;
		String filePath = "/home/jongho/Spring_project/CJU_STT/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/wtpwebapps/CJU_Web_Application_Services/resources/download/"; // 파일 저장 위치 (절대 경로로 지정해야함)
		saveAudioToFile(audioData, filePath, fileName);
		convertAudioToText(fileName, filePath, session);
	}
	
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) {
		fileName = message.getPayload();
    }
	
	// 수신 받을 음성파일 저장
	private void saveAudioToFile(byte[] audioData, String filePath, String fileName) {
		try (FileOutputStream fos = new FileOutputStream(filePath+fileName)) {
			fos.write(audioData);
			System.out.println("[WebSocketHandlerForAPI] 음성 파일이 저장 위치 : " + filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// 클라이언트로 메시지 전송
	private void sendTextMessage(WebSocketSession session, String text) throws IOException {
		WebSocketMessage<String> textMessage = new TextMessage(text);
		session.sendMessage(textMessage);
	}
	
	// 음성파일 텍스트 변환
	private void convertAudioToText(String fileName, String receiveFilePath, WebSocketSession session) throws IOException {
		System.out.println("[WebSocketHandlerForAPI] convertAudioToText()");
		
		// 파일 설정
		String orgFileName 	  = fileName;
		String baseName 		  = System.currentTimeMillis() + FilenameUtils.getBaseName(orgFileName);
		String realPath 		  = receiveFilePath;
		String filePath 		  = realPath + File.separator;
		String savePcmFilePath = filePath + baseName + ".pcm";
		String audioFilePath   = filePath  + fileName;
		String audioContents   = null;
		
		Gson gson = new Gson();
		
		Map<String, Object> request = new HashMap<>();
		Map<String, String> argument = new HashMap<>();
		try {
			String ffprobeCommand = "ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 "
					+ audioFilePath;
			ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", ffprobeCommand);
			Process process = processBuilder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String output = reader.readLine();
			
			if (output != null) {	// 음성 파일 길이가 산출되었을 경우
				double durationInSeconds = Double.parseDouble(output);
				System.out.println("[WebSocketHandlerForAPI] 음성파일 길이 : " + durationInSeconds);
				
				// 20초가 넘어가는 음성파일
				if (durationInSeconds > 20) {
					System.out.println("[WebSocketHandlerForAPI] 20초 이상의 파일입니다. 분할을 시작합니다.");
					String outputFormat = baseName  + "%d.mp3";
					String ffmpegCommand = String.format("ffmpeg -i %s -f segment -segment_time 18 -c copy %s",
							audioFilePath, filePath + outputFormat);
					try {
						ProcessBuilder processBuilder2 = new ProcessBuilder("bash", "-c", ffmpegCommand);
						Process process2 = processBuilder2.start();
						process2.waitFor();

						int countFile = (int) Math.ceil(durationInSeconds / 18);
						String resultText = null;
						FFmpeg ffmpeg = new FFmpeg("/usr/bin/ffmpeg");
						
						// 분할 개수 만큼 API 요청 실행
						for (int i = 0; i <= countFile; i++) {
							resultText = convertText(savePcmFilePath, audioContents, filePath + baseName + i + ".mp3" ,
							argument, languageCode, request, openApiURL, accessKey, gson, ffmpeg);
							sendTextMessage(session, resultText);
						}
						System.out.println("[WebSocketHandlerForAPI] Audio splitting completed.");
						session.close();
						
					} catch (IOException | InterruptedException e) {
						e.printStackTrace();
						sendTextMessage(session, "음성파일을 분할하는 과정에서 문제가 발생하였습니다.");
						session.close();
					}
				} else { // 20초가 넘어가지 않는 음성파일
					System.out.println("[WebSocketHandlerForAPI] 20초 미만의 파일입니다.");
					
					// API 요청 실행
					try {
						FFmpeg ffmpeg = new FFmpeg("/usr/bin/ffmpeg");
						String resultText = convertText(savePcmFilePath, audioContents, audioFilePath,
						argument, languageCode, request, openApiURL, accessKey, gson, ffmpeg);
						sendTextMessage(session, resultText);
						session.close();
						
					} catch (IOException e) {
						e.printStackTrace();
						delFile(audioFilePath);
						delFile(savePcmFilePath);
						sendTextMessage(session, "API 요청 과정에서 문제가 발생하였습니다.");
						session.close();
					} 
				}	
			} else {
				System.out.println("[WebSocketHandlerForAPI] 음성 길이 계산 실패");
				delFile(audioFilePath);
				sendTextMessage(session, "음성파일의 길이를 계산하는데 문제가 발생하였습니다.");
				session.close();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			sendTextMessage(session, "음성파일 변환과정에서 문제가 발생하였습니다.");
			session.close();
			delFile(audioFilePath);
		}
	}
	
	// 파일 삭제
	private void delFile(String audioFilePath) {
		File delFile = new File(audioFilePath);
		if (delFile.exists()) {
			delFile.delete();
			System.out.println("[WebSocketHandlerForAPI] (음성파일 파일삭제 성공)");
		} else {
			System.out.println("[WebSocketHandlerForAPI] (음성파일이 존재하지 않습니다.)");
		}
	}
	
	// API서버에 요청
	private String convertText(String savePcmFilePath, String audioContents, String audioFilePath,
			Map<String, String> argument, String languageCode, Map<String, Object> request, String openApiURL,
			String accessKey, Gson gson, FFmpeg ffmpeg) throws MalformedURLException, ProtocolException, IOException{
		
		// mp3파일을 PCM형식으로 변환
		try {
			mp3ToPcm(ffmpeg, audioFilePath, savePcmFilePath);
			System.out.println("[WebSocketHandlerForAPI] (Make PCM File SUCCESS)");
		} catch (IOException e) {
			e.printStackTrace();
			return "파일 PCM 변환 과정에서 문제가 발생하였습니다.";
		}
		
		// 음성파일 바이트 변환
		try {
			Path path = Paths.get(savePcmFilePath);
			byte[] audioBytes = Files.readAllBytes(path);
			audioContents = Base64.getEncoder().encodeToString(audioBytes);
			System.out.println("[WebSocketHandlerForAPI] (음성파일 바이트 변환 성공)");
			
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

		System.out.println("[WebSocketHandlerForAPI] [responseCode] " + responseCode);
		System.out.println("[WebSocketHandlerForAPI] [resultText] " + recognizedText);
		
		delFile(audioFilePath);
		delFile(savePcmFilePath);
	
		return recognizedText;
	}
	
	// mp3 파일을 PCM 형식으로 변환
	private void mp3ToPcm(FFmpeg ffmpeg, String audioFilePath, String savePcmFilePath) throws IOException {
		
		// FFmpeg 실행
		FFmpegBuilder builder = new FFmpegBuilder().setInput(audioFilePath).addOutput(savePcmFilePath)
				.setAudioCodec("pcm_s16le").setFormat("s16le").setAudioChannels(1).setAudioSampleRate(16000).done();
		FFmpegExecutor executor = new FFmpegExecutor(ffmpeg);
		executor.createJob(builder).run();
	}
}
