package com.cju.cju_stt;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {
	
	@Autowired
	FileService fileService;
	
	// 메인 페이지	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home() {
		System.out.println("[HomeController] home()");
		
		String nextPage = "mainPage";
		
		return nextPage;
	}
	
	// 서비스 페이지 요청 
	@GetMapping("/service")
	public String service() {
		System.out.println("[HomeController] service()");
		
		String nextPage = "servicePage";
		
		return nextPage;
	}
	
	// 파일 업로드 및 변환 작업 
	@RequestMapping(value = "/upload", method = {RequestMethod.POST}, produces = "application/text; charset=utf8")
	@ResponseBody
	public String fileUpload(@RequestParam(value="file") MultipartFile file, RedirectAttributes redirectAttributes) {
		System.out.println("[HomeController] fileUpload()");
		
		String result = fileService.convertAudioToText(file);
		/* System.out.println("apiCheck 결과 : " + apiCheck); */
		
		return "{ \"result\": \" " + result + " \" }";	
	}
	
	// API 파일 업로드 및 변환 작업
	@RequestMapping(value = "/uploadToAPI", method = {RequestMethod.POST}, produces = "application/text; charset=utf8")
	@ResponseBody
	public String apiFileUpload(@RequestParam(value="file") MultipartFile file, RedirectAttributes redirectAttributes) {
		System.out.println("[HomeController] apiFileUpload()");
		
		String result = fileService.convertAudioToText_API(file);
		
		return "{ \"result\": \" " + result + " \" }";	
	}
	
	// API 사용 페이지 요청
	/*
	 * @GetMapping("/serviceAPI") public String serviceAPI() {
	 * System.out.println("[HomeController] serviceAPI()");
	 * 
	 * String nextPage = "servicePageAPI";
	 * 
	 * return nextPage; }
	 */
	
	
}
