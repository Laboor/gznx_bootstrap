package com.gznx.controller;

import com.gznx.response.CommonResp;
import com.gznx.service.ExecutorService;
import com.gznx.websocket.Constant;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
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
    public CommonResp list(@RequestParam Map<String, String> req) {
        Map<String, String> startTimeMap = executorService.getStartTimeMap();
        List<Map<String, String>> resultList = new ArrayList<>();
        for (String command : startTimeMap.keySet()) {
            Map<String, String> result = new HashMap<>();
            int index = command.indexOf(Constant.COMMAND_SEPARATOR);
            result.put("command", command.substring(index + 1));
            result.put("type", command.substring(0, index));
            result.put("startTime", startTimeMap.get(command));
            resultList.add(result);
        }
        CommonResp<List> resp = new CommonResp<>();
        resp.setData(resultList);
        resp.setSuccess(true);
        resp.setMessage("获取成功");
        return resp;
    }

    @PostMapping("/exec")
    public CommonResp execCommand(@RequestBody Map<String, String> req) {
        String userId = req.get("userId");
        String command = req.get("command");
        String type = req.get("type");
        String charser = req.get("charser");
        Map result = null;
        if (StringUtils.hasText(userId) && StringUtils.hasText(command) && StringUtils.hasText(type)) {
            if (!StringUtils.hasText(charser)) {
                charser = "utf-8";
            }
            result = executorService.execCommand(userId, command, type, charser);
        }
        CommonResp<Map> resp = new CommonResp<>();
        resp.setSuccess((boolean) result.get("success"));
        resp.setMessage((String) result.get("message"));
        return resp;
    }

    @PostMapping("/interrupt")
    public CommonResp interruptShell(@RequestBody Map<String, String> req) {
        String command = req.get("command");
        String type = req.get("type");
        boolean result = false;
        if (StringUtils.hasText(command) && StringUtils.hasText(type)) {
            result = executorService.interruptedExec(type + Constant.COMMAND_SEPARATOR + command);
        }
        CommonResp<Map> resp = new CommonResp<>();
        String msg = result ? "脚本执行中断成功！" : "脚本执行中断失败，不存在该任务或任务已中断！";
        resp.setSuccess(result);
        resp.setMessage(msg);
        return resp;
    }
}
