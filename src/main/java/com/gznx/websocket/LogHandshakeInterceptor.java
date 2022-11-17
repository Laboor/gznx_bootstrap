package com.gznx.websocket;

import com.gznx.utils.Md5Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Map;

public class LogHandshakeInterceptor extends HttpSessionHandshakeInterceptor {
    private static final Logger LOG = LoggerFactory.getLogger(LogHandshakeInterceptor.class);

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest serverHttpRequest = (ServletServerHttpRequest) request;
            String type = serverHttpRequest.getServletRequest().getParameter("type");
            String command = serverHttpRequest.getServletRequest().getParameter("command");

            if (StringUtils.hasText(type) && StringUtils.hasText(command)) {
                String hash = Md5Utils.hash(type + command);
                attributes.put(Constant.WEBSOCKET_NAME, hash);
            } else {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception e) {
    }
}
