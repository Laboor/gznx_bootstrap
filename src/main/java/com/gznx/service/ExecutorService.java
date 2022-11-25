package com.gznx.service;

import com.gznx.domain.CommandInfo;
import com.gznx.utils.Helper;
import com.gznx.utils.Md5Utils;
import com.gznx.websocket.LogMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ExecutorService {
    private static final Logger LOG = LoggerFactory.getLogger(ExecutorService.class);

    private Map<String, CommandInfo> commandInfoMap = new ConcurrentHashMap<>();

    public Map<String, CommandInfo> getCommandInfoMap() {
        return commandInfoMap;
    }

    @Resource
    private LogMessageHandler logMessageHandler;

    // 新建线程处理脚本
    private void process(CommandInfo commandInfo, InputStream is, Process process) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                LOG.info("开始执行：" + commandInfo.getCommandStr());
                int exitValue = 1;
                try {
                    int retry = 5;
                    WebSocketSession session = logMessageHandler.getUserMap().get(commandInfo.getId());
                    // 尝试获取websocket session
                    while (session == null && retry > 0) {
                        Thread.sleep(500);
                        session = logMessageHandler.getUserMap().get(commandInfo.getId());
                        retry--;
                    }
                    // 是否传输日志信息
                    if (session != null && session.isOpen()) {
                        String charset = commandInfo.getCharset();
                        if (!StringUtils.hasText(charset)) {
                            charset = "UTF-8";
                        }
                        BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName(charset)));
                        String line = null;
                        while ((line = br.readLine()) != null && process.isAlive()) {
                            // 中断处理
                            if (Thread.currentThread().isInterrupted()) {
                                throw new InterruptedException("线程被手动中断");
                            }
                            if (line != null) {
                                boolean result = logMessageHandler.sendMessageToUser(commandInfo.getId(), line);
                                if (!result) break;
//                                LOG.info(line);
                            }
                        }
                    }
                    // 等待脚本执行完毕
                    exitValue = process.waitFor();
                    LOG.info(commandInfo.getCommandStr() + " 执行完毕！exit code: " + exitValue);
                } catch (IOException e) {
                    LOG.error("日志流读取异常！", e);
                } catch (InterruptedException e) {
                    LOG.error(commandInfo.getCommandStr() + " 执行线程中断！", e);
                } finally {
                    commandInfoMap.remove(commandInfo.getId());
                    try {
                        is.close();
                        if (process != null && process.isAlive()) {
                            process.getInputStream().close();
                            process.getErrorStream().close();
                            process.getOutputStream().close();
                            process.destroy();
                        }
                    } catch (IOException e) {
                        LOG.error("日志流关闭异常！", e);
                    }
                }
            }
        });
        thread.start();
        commandInfo.setExecThread(thread);
        commandInfo.setProcess(process);
        commandInfo.setStartTime(Helper.dateFormat(new Date()));
        commandInfoMap.put(commandInfo.getId(), commandInfo);
    }

    // 执行脚本
    public boolean execCommand(CommandInfo commandInfo) throws IOException {
        try {
            String[] args = commandInfo.getCommandStr().replace("\\", "/").split("\\s+");
            ProcessBuilder pb = new ProcessBuilder(args);
            pb.redirectErrorStream(true); // 将错误流合并到输入流中
            Process p = pb.start();
            InputStream is = p.getInputStream();

            process(commandInfo, is, p);

            return true;
        } catch (Exception e) {
            LOG.error(commandInfo.getCommandStr() + " 脚本调用异常！", e);
            throw e;
        }
    }

    // 中断脚本执行
    public boolean interruptedExec(String commandId) throws IOException {
        CommandInfo commandInfo = commandInfoMap.get(commandId);
        if (commandInfo == null) {
            return false;
        }
        Thread thread = commandInfo.getExecThread();
        if (thread != null && !thread.isInterrupted()) {
            thread.interrupt();
            Process process = commandInfo.getProcess();
            if (process != null && process.isAlive()) {
                process.getInputStream().close();
                process.getErrorStream().close();
                process.getOutputStream().close();
                process.destroy();
            }
            return true;
        }
        return false;
    }

    // 初始化对象
    public CommandInfo initCommandInfo(CommandInfo commandInfo) {
        CommandInfo newCommandInfo = commandInfo;
        newCommandInfo.setType(commandInfo.getType());
        newCommandInfo.setCommandStr(commandInfo.getCommandStr().trim());
        newCommandInfo.setId(Md5Utils.hash(commandInfo.getType() + commandInfo.getCommandStr().trim()));
        return newCommandInfo;
    }

}
