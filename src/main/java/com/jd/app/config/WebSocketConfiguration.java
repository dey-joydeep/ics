package com.jd.app.config;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import org.springframework.web.util.WebUtils;

import com.jd.app.shared.constant.general.AppConstants;
import com.jd.app.websocket.handler.MessageHandler;

import lombok.extern.log4j.Log4j2;

/**
 * @author Joydeep Dey
 */
@Log4j2
@Async
@Configuration
@EnableWebSocket
public class WebSocketConfiguration implements WebSocketConfigurer {

	@Autowired
	protected MessageHandler webSocketController;

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		String socketPath = "/socket/conn/";
		// FIXME restrict origin to same domain
		registry.addHandler(webSocketController, socketPath).addInterceptors(new HttpSessionHandshakeInterceptor())
				.setAllowedOrigins("*").addInterceptors(new UriTemplateHandshakeInterceptor());
		log.info("Socket registered to the path: " + socketPath + "\t" + registry);
	}

	/**
	 * @return websocket container
	 */
	@Bean
	public ServletServerContainerFactoryBean createWebSocketContainer() {
		ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
		container.setMaxBinaryMessageBufferSize(26214400);
		log.info("Socket container created.");
		return container;
	}

	private class UriTemplateHandshakeInterceptor implements HandshakeInterceptor {

		@Override
		public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
				WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

			/* Retrieve original HTTP request */
			HttpServletRequest origRequest = ((ServletServerHttpRequest) request).getServletRequest();
			String username = WebUtils.findParameterValue(origRequest, "username");
			String query = origRequest.getQueryString();

			if (StringUtils.isBlank(query) && query.contains("="))
				return false;
			if (StringUtils.isBlank(username))
				return false;

			Object sUsername = attributes.get(AppConstants.SESSION_ATTR_USERNAME);
			return username.equals(sUsername);
		}

		@Override
		public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
				Exception exception) {
		}

	}

}
