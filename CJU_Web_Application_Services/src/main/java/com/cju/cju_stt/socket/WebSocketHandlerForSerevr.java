package com.cju.cju_stt.socket;

import java.io.FileOutputStream;
import java.io.IOException;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

public class WebSocketHandlerForSerevr extends BinaryWebSocketHandler {
	
	protected String fileName;
	
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
	private void convertAudioToText(String fileName, String receiveFilePath, WebSocketSession session) {
		System.out.println("[WebSocketHandlerForSerevr] convertAudioToText()");
		
		// 서버를 사용하는 변환 코드 작성
	}

}
