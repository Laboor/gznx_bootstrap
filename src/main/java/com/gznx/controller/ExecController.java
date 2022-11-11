package com.gznx.controller;

import com.gznx.response.CommonResp;
import com.gznx.service.ExecutorService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/shell")
public class ExecController {
    @Resource
    private ExecutorService executorService;

    @GetMapping("/list")
    public CommonResp list(@RequestParam Map<String, String> req) {
        Map<String, String> startTimeMap = executorService.getStartTimeMap();
        List<Map<String, String>> resultList = new ArrayList<>();
        for (String shellPath : startTimeMap.keySet()) {
            Map<String, String> result = new HashMap<>();
            result.put("path", shellPath);
            result.put("startTime", startTimeMap.get(shellPath));
            resultList.add(result);
        }
        CommonResp<List> resp = new CommonResp<>();
        resp.setData(resultList);
        resp.setSuccess(true);
        resp.setMessage("获取成功");
        return resp;
    }

    @PostMapping("/exec")
    public CommonResp execShell(@RequestBody Map<String, String> req) {
        String userId = req.get("userId");
        String shellPath = req.get("shellPath");
        String charser = req.get("charser");
        Map result = null;
        if (StringUtils.hasText(userId) && StringUtils.hasText(shellPath)) {
            if (!StringUtils.hasText(charser)) {
                charser = "utf-8";
            }
            result = executorService.execShell(userId, shellPath, charser);
        }
        CommonResp<Map> resp = new CommonResp<>();
        resp.setSuccess((boolean) result.get("success"));
        resp.setMessage((String) result.get("message"));
        return resp;
    }

    @PostMapping("/interrupt")
    public CommonResp interruptShell(@RequestBody Map<String, String> req) {
        String shellPath = req.get("shellPath");
        boolean result = false;
        if (StringUtils.hasText(shellPath)) {
            result = executorService.interruptedExec(shellPath);
        }
        CommonResp<Map> resp = new CommonResp<>();
        String msg = result ? "脚本执行中断成功！" : "脚本执行中断失败，不存在该任务或任务已中断！";
        resp.setSuccess(result);
        resp.setMessage(msg);
        return resp;
    }
}
