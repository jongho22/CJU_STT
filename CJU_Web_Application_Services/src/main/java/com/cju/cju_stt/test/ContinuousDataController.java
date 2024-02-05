package com.cju.cju_stt.test;

import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

@RestController
@RequestMapping("/sse")
public class ContinuousDataController {
	
//	@Autowired
//	TestService testService;
//	
//	@GetMapping
//	public SseEmitter streamSseMvc() {
//	    SseEmitter emitter = new SseEmitter();
//	    ExecutorService sseMvcExecutor = Executors.newSingleThreadExecutor();
//	    sseMvcExecutor.execute(() -> {
//	        try {
//	            for (int i = 0; i < 20; i++) {
//	                SseEventBuilder event = SseEmitter.event()
//	                        .data(System.currentTimeMillis());
//	                emitter.send(event);
//	                Thread.sleep(1000);
//	            }
//	        } catch (Exception ex) {
//	            emitter.completeWithError(ex);
//	        }
//	    });
//	    return emitter;
//	}
	
	
	 private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamText() {
        SseEmitter emitter = new SseEmitter();
        emitters.add(emitter);

        // 클라이언트 연결이 종료되면 이벤트 리스너에서 제거
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onError((ex) -> emitters.remove(emitter));

        return emitter;
    }
	    
	 // 예를 들어, 주기적으로 데이터를 생성하고 클라이언트에게 전송하는 메서드
    private void sendTextToClients(String text) {
        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event().data(text));
            } catch (IOException e) {
                // 예외 처리
            }
        });
    }
	    
	 // 주기적으로 데이터를 생성하고 클라이언트에게 전송하는 예제 메서드
    private void simulateDataGeneration() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000); // 1초마다 데이터 생성 및 전송
                    String generatedText = "Generated text at " + System.currentTimeMillis();
                    sendTextToClients(generatedText);
                    System.out.println(generatedText);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
	
}