package com.jd.app.websocket.bean;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.socket.WebSocketSession;

/**
 * @author Joydeep Dey
 */
public class GlobalResource {
	/***/
	public static Map<String, List<WebSocketSession>> ACTIVE_SESSION_HOLDER = new ConcurrentHashMap<>();
	public static Set<String> OFFLINE_REQUEST_HOLDER = ConcurrentHashMap.newKeySet();
}
