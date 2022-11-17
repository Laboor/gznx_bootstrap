package com.gznx.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LogMessageHandler implements WebSocketHandler {
    private static final Logger LOG = LoggerFactory.getLogger(LogMessageHandler.class);
    private static final Map<String, WebSocketSession> userMap = new ConcurrentHashMap<>();

    public static Map<String, WebSocketSession> getUserMap() {
        return userMap;
    }

    // 建立websocket时调用
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = getUserId(session);
        if (StringUtils.hasText(userId)) {
            userMap.put(userId, session);
            LOG.info("用户[" + userId + "]成功建立连接！");
            session.sendMessage(new TextMessage("建立服务端连接成功！"));
        } else {
            LOG.error("用户ID为空，无法建立连接！");
        }
    }

    // 客户端发送消息时调用
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String msg = message.getPayload().toString();
        String userId = getUserId(session);
        LOG.info("收到用户[" + userId + "]发送的消息：" + msg);
        message = new TextMessage("服务端已接收到消息，msg=" + msg);
        session.sendMessage(message);
    }

    // 向单个用户发送消息
    public boolean sendMessageToUser(String userId, String message) {
        WebSocketSession session = userMap.get(userId);
        if (session != null && session.isOpen()) {
            try {
                TextMessage msg = new TextMessage(message);
                session.sendMessage(msg);
                return true;
            } catch (IOException e) {
                LOG.error("向用户[" + userId + "]发送消息失败！", e);
                return false;
            }
        } else {
            LOG.error("用户[" + userId + "]session不存在或未开启！");
            return false;
        }
    }

    // 向所有用户发送消息
    public void sendMessageToAllUsers(String message) {
        Set<String> userIds = userMap.keySet();
        for (String userId : userIds) {
            this.sendMessageToUser(userId, message);
        }
    }

    // 传输过程出现异常时调用
    @Override
    public void handleTransportError(WebSocketSession session, Throwable e) throws Exception {
        String userId = getUserId(session);
        userMap.remove(userId);
        WebSocketMessage<String> message = new TextMessage("传输过程中出现异常！");
        session.sendMessage(message);
        LOG.error("与用户[" + userId + "]传输过程中出现异常！", e);
        if (session.isOpen()) {
            session.close();
        }
    }

    // 关闭websocket时调用
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        String userId = getUserId(session);
        if (StringUtils.hasText(userId)) {
            userMap.remove(userId);
            LOG.info("用户[" + userId + "]连接已成功关闭！");
        } else {
            LOG.error("用户ID为空，无法关闭连接！");
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private String getUserId(WebSocketSession session) {
        try {
            String userId = (String) session.getAttributes().get(Constant.WEBSOCKET_NAME);
            return userId;
        } catch (Exception e) {
            LOG.error("获取websocket用户信息失败！", e);
        }
        return null;
    }
}
