package com.jd.app.config;

import static com.jd.app.shared.constant.general.AppConstants.SESSION_ATTR_USERNAME;
import static com.jd.app.shared.constant.general.AppConstants.WS_SESSION_ATTR_ID;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import javax.websocket.CloseReason;

import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import com.jd.app.modules.login.service.LoginService;
import com.jd.app.shared.helper.SessionBean;
import com.jd.app.websocket.bean.WebSocketSessionResource;

import lombok.extern.log4j.Log4j2;

/**
 * @author Joydeep Dey
 */
@Log4j2
@Component
public class SessionListener implements HttpSessionListener {

	public void sessionCreated(HttpSessionEvent se) {
		log.info("Session created for user: " + se.getSession().getAttribute(SESSION_ATTR_USERNAME));
	}

	public void sessionDestroyed(HttpSessionEvent se) {
		LoginService loginService = getLoginService(se);
		HttpSession session = se.getSession();
		SessionBean sessionBean = new SessionBean(session);
		loginService.processLogout(sessionBean);
		String username = sessionBean.getUserName();
		session.removeAttribute(SESSION_ATTR_USERNAME);
		WebSocketSession wsSession = null;
		List<WebSocketSession> sessionList = WebSocketSessionResource.ACTIVE_SESSION_HOLDER.get(username);
		Iterator<WebSocketSession> sessionIterator = sessionList.iterator();
		while (sessionIterator.hasNext()) {
			WebSocketSession ws = sessionIterator.next();
			if (ws.getAttributes().get(WS_SESSION_ATTR_ID).equals(session.getId())) {
				wsSession = ws;
				break;
			}
		}

		if (wsSession == null) {
			log.info("No socket session is found to close for user: " + username);
			return;
		}

		CloseStatus closeStatus = new CloseStatus(CloseReason.CloseCodes.NORMAL_CLOSURE.getCode());
		try {
			wsSession.close(closeStatus);
		} catch (IOException e) {
			log.error("Cannot close socket session for user: " + username, e);
		}
		log.info("Socket session closed for user: " + username + " with close status: " + closeStatus);
	}

	private static LoginService getLoginService(HttpSessionEvent se) {
		WebApplicationContext context = WebApplicationContextUtils
				.getWebApplicationContext(se.getSession().getServletContext());
		return (LoginService) context.getBean(LoginService.class);
	}

}
