package com.cju.cju_stt.socket;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(new MyWebSocketHandler(), "/my-websocket");
	}

	@Controller
	public static class MyWebSocketHandler extends TextWebSocketHandler {
		@Override
		protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
			// 웹소켓으로 전송된 데이터를 처리하는 로직을 작성합니다.
			byte[] data = message.asBytes();
			String textData = message.toString();
			System.out.println("클라이언트에서 보낸 바이트 데이터 : " + data);
			System.out.println("클라이언트에서 보낸 텍스트 데이터 : " + textData);
			// 처리된 데이터를 원하는 대로 활용할 수 있습니다.
		}
	}
}