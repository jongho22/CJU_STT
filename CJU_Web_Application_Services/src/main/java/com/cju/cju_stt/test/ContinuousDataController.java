package com.cju.cju_stt.test;

import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RestController
@RequestMapping("/sse")
public class ContinuousDataController {

	@Autowired
	TestService testService;

	@RequestMapping(value = "/uploadToAPI", method = { RequestMethod.POST,
			}, produces = "application/text; charset=utf8")
	public SseEmitter streamSseMvc(@RequestParam(value="file") MultipartFile file, RedirectAttributes redirectAttributes) {
		System.out.println("[ContinuousDataController] streamSseMvc()");
		
		 SseEmitter result = testService.convertAudioToText_API(file);
		
		return result;
	}
	
//	@RequestMapping(value = "/uploadToAPI", method = {
//			RequestMethod.GET }, produces = "application/text; charset=utf8")
//	public SseEmitter streamSseMvc2() {
//		System.out.println("[ContinuousDataController] streamSseMvc2()");
//		
//		
//		
//		return result;
//	}

}