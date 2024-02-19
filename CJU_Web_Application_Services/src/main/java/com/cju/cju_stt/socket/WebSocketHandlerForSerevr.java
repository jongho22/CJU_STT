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
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import com.google.gson.Gson;

public class WebSocketHandlerForSerevr extends BinaryWebSocketHandler {
	
	protected String fileName;
	
	@Override
	protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
		byte[] audioData = message.getPayload().array();
		fileName = System.currentTimeMillis()+fileName;
		String filePath = "/home/jongho/Spring_project/CJU_STT/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/wtpwebapps/CJU_Web_Application_Services/resources/"; // 파일 저장 위치 (절대 경로로 지정해야함)
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
		
		Map<String, Object> request = new HashMap<>();
		Map<String, String> argument = new HashMap<>();
		
		argument.put("filePath", filePath+fileName);
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
			System.out.println("[WebSocketHandlerForSerevr] [resultText] " + recognizedText);
			
			// 변환 결과 출력
			delFile(filePath);
			sendTextMessage(session, recognizedText);
			session.close();
			
		} catch (Exception e) {
			delFile(filePath);
			sendTextMessage(session, "음성파일 변환과정에서 문제가 발생하였습니다.");
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
}
