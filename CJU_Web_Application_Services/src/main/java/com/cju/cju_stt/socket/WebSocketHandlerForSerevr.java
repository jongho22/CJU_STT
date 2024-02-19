package com.cju.cju_stt.socket;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

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
	private void convertAudioToText(String fileName, String receiveFilePath, WebSocketSession session) {
		System.out.println("[WebSocketHandlerForSerevr] convertAudioToText()");
		
		// 서버를 사용하는 변환 코드 작성
		String resultText;
		String orgFileName   = fileName;								// 음성파일 이름 (확장자 포함)
		String realPath      = receiveFilePath;						// 다운로드 경로
		String filePath      = realPath + File.separator;		 	// 파일 경로
		String audioFilePath = filePath + orgFileName;				// 사용하게 되는 파일 경로
		String PYTHON_PATH   = receiveFilePath + "/python/main.py";	// 실행 파이썬 코드
		
		ProcessBuilder processBuilder = new ProcessBuilder("python3", PYTHON_PATH, orgFileName, filePath);
		Process process;
		
		try {
			process = processBuilder.start();
			System.out.println("[WebSocketHandlerForSerevr] (Start process) => " + orgFileName);

			try {
				int exitval = process.waitFor();
				// InputStream inputStream = process.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));

				// STT 결과 출력
				while ((resultText = br.readLine()) != null) {
					System.out.println("[WebSocketHandlerForSerevr] (SUCCESS) => " + orgFileName);
					sendTextMessage(session, resultText);
					session.close();
				}
				System.out.println("[WebSocketHandlerForSerevr] (** FAIL **) => " + orgFileName);
				sendTextMessage(session, "결과를 출력하는 과정에서 문제가 발생하였습니다.");
				session.close();

			} catch (InterruptedException e) {
				e.printStackTrace();
				sendTextMessage(session, "결과를 출력하는 과정에서 문제가 발생하였습니다.");
				session.close();
			}

		} catch (Exception e) {
			System.out.println("[WebSocketHandlerForSerevr] Fail processBuilder start.");
			e.printStackTrace();
//			sendTextMessage(session, "음성파일을 텍스트로 변환하는 과정에서 문제가 발생하였습니다.");
//			session.close();
//			return "음성파일을 텍스트로 변환하는 과정에서 문제가 발생하였습니다.";

		} finally {
			// 사용한 파일 삭제
			File delFile = new File(audioFilePath);
			
			if (delFile.exists()) {
				delFile.delete();
				System.out.println("[WebSocketHandlerForSerevr] (파일삭제 성공) => " + orgFileName);
			} else {
				System.out.println("[WebSocketHandlerForSerevr] (파일이 존재하지 않습니다.)");
			}
		}
	}

}
