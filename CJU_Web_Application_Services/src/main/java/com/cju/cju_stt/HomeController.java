package com.cju.cju_stt;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

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
		
		String nextPage = "base";
		
		return nextPage;
	}
	
	@GetMapping("/service")
	public String service() {
		System.out.println("[HomeController] service()");
		
		String nextPage = "servicePage";
		
		return nextPage;
	}
	
	@PostMapping("/upload")
	public String fileUpload(@RequestParam(value="file") MultipartFile file, RedirectAttributes redirectAttributes) {
		System.out.println("[HomeController] fileUpload()");
		
		String result = fileService.convertAudioToText(file);
		System.out.println(result);
		redirectAttributes.addFlashAttribute("message", result);
		
		String nextPage = "redirect:/result";
		
		return nextPage;
	}
	
	@GetMapping("result")
	public String loading(@ModelAttribute("message") String message, Model model) {
		System.out.println("[HomeController] result()");
		
		String nextPage = "result";
		
		model.addAttribute("message", message);
		
		return nextPage;
	}
}
