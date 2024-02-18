package com.cju.cju_stt.socket;


import java.io.FileOutputStream;
import java.io.IOException;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

public class AudioWebSocketHandler extends BinaryWebSocketHandler {
	
	String text;
		
	@Override
	protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
		byte[] audioData = message.getPayload().array();
		saveAudioToFile(audioData, text);
		sendTextMessage(session, "서버에서 음성파일을 성공적으로 저장했습니다.");
	}
	
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        text = message.getPayload();
    }
	
	private void saveAudioToFile(byte[] audioData, String fileName) {
		// 파일로 저장
		try (FileOutputStream fos = new FileOutputStream(fileName)) {
			fos.write(audioData);
			System.out.println("[AudioWebSocketHandler] 음성 파일이 저장되었습니다 : " + fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void sendTextMessage(WebSocketSession session, String text) throws IOException {
		WebSocketMessage<String> textMessage = new TextMessage(text);
		session.sendMessage(textMessage);
	}
}
