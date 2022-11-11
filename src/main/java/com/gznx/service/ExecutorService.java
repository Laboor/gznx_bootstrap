package com.gznx.service;

import com.gznx.websocket.LogMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ExecutorService {
    private static final Logger LOG = LoggerFactory.getLogger(ExecutorService.class);
    private Map<String, Thread> threadMap = new ConcurrentHashMap<>();
    private Map<String, String> startTimeMap = new ConcurrentHashMap<>();

    public Map<String, Thread> getThreadMap() {
        return threadMap;
    }
    public Map<String, String> getStartTimeMap() {
        return startTimeMap;
    }

    @Resource
    private LogMessageHandler logMessageHandler;

    private void process(String userId, String shellPath, InputStream is, String charser, Process p) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                LOG.info("开始执行：" + shellPath);
                int exitValue = 1;
                try {
                    int retry = 5;
                    WebSocketSession session = logMessageHandler.getUserMap().get(userId);
                    // 尝试获取websocket session
                    while (session == null && retry > 0) {
                        Thread.sleep(500);
                        session = logMessageHandler.getUserMap().get(userId);
                        retry--;
                    }
                    // 是否传输日志信息
                    if (session != null && session.isOpen()) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName(charser)));
                        String line = null;
                        while ((line = br.readLine()) != null) {
                            // 中断处理
                            if (Thread.currentThread().isInterrupted()) {
                                throw new InterruptedException("线程被手动中断");
                            }
                            if (line != null) {
                                boolean result = logMessageHandler.sendMessageToUser(userId, line);
                                if (!result) break;
                                LOG.info(line);
                            }
                        }
                    }
                    // 等待脚本执行完毕
                    exitValue = p.waitFor();
                    LOG.info(shellPath + " 执行完毕！exit code: " + exitValue);
                } catch (IOException e) {
                    LOG.error("日志流读取异常！", e);
                } catch (InterruptedException e) {
                    LOG.error(shellPath + " 执行线程中断！", e);
                } finally {
                    threadMap.remove(shellPath);
                    startTimeMap.remove(shellPath);
                    try {
                        is.close();
                        if (p != null) {
                            p.getInputStream().close();
                            p.getErrorStream().close();
                            p.getOutputStream().close();
                            p.destroy();
                        }
                    } catch (IOException e) {
                        LOG.error("日志流关闭异常！", e);
                    }
                }
            }
        });
        thread.start();
        threadMap.put(shellPath, thread);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        startTimeMap.put(shellPath, dateFormat.format(new Date()));
    }


    public Map execShell(String userId, String shellPath, String charser) {
        String[] shellArgs = shellPath.replace("\\", "/").split("\\s+");
        Map result = new HashMap();
        if (threadMap.get(shellPath) != null) {
            result.put("success", false);
            result.put("message", "脚本正在执行，请勿重复调用！");
            return result;
        }
        try {
            ProcessBuilder pb = new ProcessBuilder(shellArgs);
            pb.redirectErrorStream(true); // 将错误流合并到输入流中
            Process p = pb.start();
            InputStream is = p.getInputStream();

            process(userId, shellPath, is, charser, p);

            result.put("success", true);
            result.put("message", "脚本调用成功！");
            return result;
        } catch (Exception e) {
            LOG.error(shellPath + " 脚本调用异常！", e);
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }

    // 中断脚本执行
    public boolean interruptedExec(String shellPath) {
        Thread thread = threadMap.get(shellPath);
        if (thread != null && !thread.isInterrupted()) {
            thread.interrupt();
            return true;
        }
        return false;
    }
}
