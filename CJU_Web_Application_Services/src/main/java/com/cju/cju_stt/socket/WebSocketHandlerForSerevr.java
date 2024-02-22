package com.cju.cju_stt.socket;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import com.google.gson.Gson;

import net.bramp.ffmpeg.FFmpeg;

public class WebSocketHandlerForSerevr extends BinaryWebSocketHandler {
	
	protected String fileName;
	
	@Override
	protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
		byte[] audioData = message.getPayload().array();
		fileName = System.currentTimeMillis()+fileName;
		String filePath = "/home/jongho/Spring_project/CJU_STT/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/wtpwebapps/CJU_Web_Application_Services/resources/download/"; // 파일 저장 위치 (절대 경로로 지정해야함)
		saveAudioToFile(audioData, filePath, fileName);
		try {
			convertAudioToText(fileName, filePath, session);
		} catch(Exception e){
			System.out.println("[WebSocketHandlerForSerevr] 사용자가 페이지를 이탈하였습니다.");
			// 원본 음성 파일 삭제
			delFile(filePath + fileName); 
			
			// 분할된 음성데이터 폴더 삭제
			File delFile = new File(filePath+FilenameUtils.getBaseName(fileName)+"/");
			File[] deleteList = delFile.listFiles();
			
			for (int j = 0; j < deleteList.length; j++  ) {
				deleteList[j].delete();
			}
			delFile.delete();
			System.out.println("[WebSocketHandlerForSerevr] 음성데이터를 정리하였습니다.");
		}
	}
	
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) {
		fileName = message.getPayload();
    }
	
	// 수신 받을 음성파일 저장
	private void saveAudioToFile(byte[] audioData, String filePath, String fileName) {
		try (FileOutputStream fos = new FileOutputStream(filePath+fileName)) {
			fos.write(audioData);
			System.out.println("[WebSocketHandlerForSerevr] 음성 파일이 저장 위치 : " + filePath);
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
	private void convertAudioToText(String fileName, String filePath, WebSocketSession session) throws IOException {
		System.out.println("[WebSocketHandlerForSerevr] convertAudioToText()");
		
		String resultText = null;		
		String orgFileName = FilenameUtils.getBaseName(fileName);
		String orgAudioFilePath = filePath + fileName;
		String audioFilePath = filePath + orgFileName + fileName;
		String ffprobeCommand = "ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 "
				+ orgAudioFilePath; // 음성파일 길이 계산 커맨드 (분할 전의 음성파일 경로)
		
		Map<String, Object> request = new HashMap<>();
		Map<String, String> argument = new HashMap<>();
		
		// 음성파일 길이 계산
		ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", ffprobeCommand);
		Process process = processBuilder.start();
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String output = reader.readLine();
		
		if (output != null) {
			double durationInSeconds = Double.parseDouble(output);
			System.out.println("[WebSocketHandlerForServer] 음성파일 길이 : " + durationInSeconds);
			
			if (durationInSeconds > 60) {
				System.out.println("[WebSocketHandlerForServer] 60초 이상의 파일입니다. 분할을 시작합니다.");
			    
				// 분할한 음성파일을 저장할 폴더 생성
				File file = new File(filePath+orgFileName+"/");
				file.mkdir();
				
				// 분할 준비
				String outputFormat = orgFileName  + "%d.mp3";
				String outputPath = filePath + orgFileName+"/" + outputFormat;
				String ffmpegCommand = String.format("ffmpeg -i %s -f segment -segment_time 60 -c copy %s",
						orgAudioFilePath, outputPath);
						// 분할 전 음성데이터, 분할 후 저장 음성파일
				
				try {
					ProcessBuilder processBuilder2 = new ProcessBuilder("bash", "-c", ffmpegCommand);
					Process process2 = processBuilder2.start();
					process2.waitFor();
					
					int countFile = (int) Math.ceil(durationInSeconds / 60);
					
					delFile(filePath + fileName); // 분할하기 전의 음성파일 삭제
					
					for (int i = 0; i <= countFile; i++) {
						resultText = convert(argument, request, filePath + orgFileName+"/" + orgFileName +i+".mp3");
						String json = "{\"resultText\" : \"" + resultText+ "\", \"percent\" : \"" + (int) Math.ceil(((double) i / countFile) * 100) + "\"}";
						sendTextMessage(session, json);
					}
					System.out.println("[WebSocketHandlerForServer] 작업이 완료되었습니다.");
					delFile(filePath+orgFileName+"/"); // 분할된 음성데이터 폴더 삭제
					session.close();
					
				} catch (IOException | InterruptedException e) {
					delFile(audioFilePath);
					sendTextMessage(session, "음성파일을 분할하는 과정에서 문제가 발생하였습니다.");
					delFile(filePath+orgFileName+"/"); // 분할된 음성데이터 폴더 삭제
					session.close();
				}
				
			} else {
				System.out.println("[WebSocketHandlerForServer] 60초 미만의 파일입니다.");
				resultText = convert(argument, request, orgAudioFilePath);
				String json = "{\"resultText\" : \"" + resultText+ "\", \"percent\" : \"100\"}";
				
				sendTextMessage(session, json);
				session.close();
			}
			
		} else { 
			System.out.println("[WebSocketHandlerForServer] 음성 길이 계산 실패");
			delFile(orgAudioFilePath);
			sendTextMessage(session, "음성파일의 길이를 계산하는데 문제가 발생하였습니다.");
			session.close();
		}
	}
	
	// 파일 삭제
	private void delFile(String audioFilePath) {
		File delFile = new File(audioFilePath);
		if (delFile.exists()) {
			delFile.delete();
			System.out.println("[WebSocketHandlerForSerevr] (음성파일 파일삭제 성공)");
		} else {
			System.out.println("[WebSocketHandlerForSerevr] (음성파일이 존재하지 않습니다.)");
		}
	}
	
	private String convert(Map<String, String> argument, Map<String, Object> request, String filePath) {
		argument.put("filePath", filePath);
		request.put("argument", argument);
		
		URL url;
		Integer responseCode = null;
		String responBody = null;
		Gson gson = new Gson();
		
		try {
			url = new URL("http://127.0.0.1:8000/uploadToFastAPI");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.write(gson.toJson(request).getBytes("UTF-8"));
			wr.flush();
			wr.close();
			
			responseCode = con.getResponseCode();
			InputStream is = con.getInputStream();
			byte[] buffer = new byte[is.available()];
			int byteRead = is.read(buffer);
			responBody = new String(buffer);
			
			Map<String, Object> responseMap = gson.fromJson(responBody, Map.class);
			String recognizedText = (String) responseMap.get("text");
			
			System.out.println("[WebSocketHandlerForSerevr] [responseCode] " + responseCode);
			//System.out.println("[WebSocketHandlerForSerevr] [resultText] " + recognizedText);
			
			// 변환 결과 출력
			delFile(filePath);
			return recognizedText;
			
		} catch (Exception e) {
			delFile(filePath);
			return "음성파일 변환과정에서 문제가 발생하였습니다.";
		}
	}
}
