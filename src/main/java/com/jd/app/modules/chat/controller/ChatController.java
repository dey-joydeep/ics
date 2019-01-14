package com.jd.app.modules.chat.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jd.app.modules.chat.bean.ChatBean;
import com.jd.app.modules.chat.service.ChatService;
import com.jd.app.shared.annotation.PostJsonMapping;

/**
 * @author Joydeep Dey
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

	@Autowired
	private ChatService chatService;

	@PostJsonMapping("/delete/units")
	public ChatBean deleteMessages(@RequestBody ChatBean chatBean) {
		chatService.delete(chatBean);
		return chatBean;
	}

	@PostJsonMapping("/delete/all")
	public void deleteConversation(@RequestBody ChatBean chatBean) {
		chatService.delete(chatBean);
	}

	@PostJsonMapping("/archive")
	public void archiveConversation() {

	}
}
