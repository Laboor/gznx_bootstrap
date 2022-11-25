package com.gznx.controller;

import com.gznx.domain.CommandInfo;
import com.gznx.response.CommonResp;
import com.gznx.service.ExecutorService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/command")
public class ExecController {
    @Resource
    private ExecutorService executorService;

    @GetMapping("/list")
    public CommonResp list() {
        Map<String, CommandInfo> commandInfoMap = executorService.getCommandInfoMap();
        List<Map<String, String>> resultList = new ArrayList<>();
        for (String key : commandInfoMap.keySet()) {
            CommandInfo commandInfo = commandInfoMap.get(key);
            Map<String, String> result = new HashMap<>();
            result.put("command", commandInfo.getCommandStr());
            result.put("type", commandInfo.getType());
            result.put("startTime", commandInfo.getStartTime());
            resultList.add(result);
        }
        CommonResp<List> resp = new CommonResp<>();
        resp.setData(resultList);
        resp.setSuccess(true);
        resp.setMessage("获取成功");
        return resp;
    }

    @PostMapping("/exec")
    public CommonResp execCommand(@Valid @RequestBody CommandInfo commandInfoReq) {
        CommandInfo commandInfo = executorService.initCommandInfo(commandInfoReq);
        CommonResp<String> resp = new CommonResp<>();
        try {
            if (executorService.getCommandInfoMap().get(commandInfo.getId()) != null) {
                resp.setSuccess(false);
                resp.setMessage("脚本正在执行，请勿重复调用！");
                return resp;
            }
            boolean result = executorService.execCommand(commandInfo);
            resp.setSuccess(result);
            resp.setMessage(result ? "脚本调用成功！" : "脚本调用异常！");
            return resp;
        } catch (Exception e) {
            resp.setSuccess(false);
            resp.setMessage("脚本调用异常，异常原因：" + e.getMessage());
            return resp;
        }
    }

    @PostMapping("/interrupt")
    public CommonResp interruptShell(@Valid @RequestBody CommandInfo commandInfoReq) throws IOException {
        CommandInfo commandInfo = executorService.initCommandInfo(commandInfoReq);
        CommonResp<Map> resp = new CommonResp<>();
        boolean result = executorService.interruptedExec(commandInfo.getId());
        String msg = result ? "脚本执行中断成功！" : "脚本执行中断失败，不存在该任务或任务已中断！";
        resp.setSuccess(result);
        resp.setMessage(msg);
        return resp;
    }
}
