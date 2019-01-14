package com.jd.app.websocket.bean;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.socket.WebSocketSession;

/**
 * Global shared resources to manipulate websocket sessions
 * 
 * @author Joydeep Dey
 */
public class WebSocketSessionResource {
	/**
	 * {@link ConcurrentHashMap} of <code>username</code> and their associated
	 * {@link WebSocketSession} list to hold active user sessions.
	 */
	public static volatile Map<String, List<WebSocketSession>> ACTIVE_SESSION_HOLDER = new ConcurrentHashMap<>();

	/**
	 * A synchronized {@link HashSet} of <code>username</code> to queue offline
	 * request on session removal.
	 */
	public static volatile Set<String> OFFLINE_REQUEST_HOLDER = ConcurrentHashMap.newKeySet();
}
