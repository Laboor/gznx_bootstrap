package com.gznx.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gznx.domain.UploadFileInfo;
import com.gznx.response.CommonResp;
import com.gznx.service.UploadService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.File;
import java.util.*;

@RestController
@RequestMapping("/file")
public class UploadController {
    @Resource
    private UploadService uploadService;

    @PostMapping("/check")
    public CommonResp fileCheck(@Valid @RequestBody UploadFileInfo uploadFileInfo) {
        CommonResp<String> resp = new CommonResp<>();
        int result = uploadService.checkFile(uploadFileInfo);
        resp.setData(String.valueOf(result));
        resp.setSuccess(true);
        resp.setMessage("请求成功");
        return resp;
    }

    @PostMapping("/upload")
    public CommonResp fileUpload(@RequestPart(value = "file") MultipartFile uploadFile, @Valid UploadFileInfo uploadFileInfo) throws Exception {
        CommonResp<String> resp = new CommonResp<>();
        String filePath = uploadService.upload(uploadFile, uploadFileInfo);
        resp.setData(filePath);
        resp.setSuccess(true);
        resp.setMessage("上传成功！");
        return resp;
    }

    @PostMapping("/merge")
    public CommonResp fileMerge(@Valid @RequestBody UploadFileInfo uploadFileInfo) throws Exception {
        CommonResp<String> resp = new CommonResp<>();
        boolean result = uploadService.fileMeger(uploadFileInfo);
        resp.setSuccess(result);
        resp.setMessage(result ? "文件合并成功！" : "文件合并失败！");
        return resp;
    }

    @PostMapping("/cancel")
    public CommonResp cancelUpload(@RequestBody Map<String, String> req) throws Exception {
        String fileListArr = req.get("fileList");
        CommonResp<String> resp = new CommonResp<>();
        Thread.sleep(1000); // 等待1秒后再删除，防止冲突
        boolean result = true;
        JSONArray fileList = JSONObject.parseArray(fileListArr);
        for (int i = 0; i < fileList.size(); i++) {
            JSONObject file = fileList.getJSONObject(i);
            String uploadDir = file.getString("uploadDir");
            String fileMd5 = file.getString("fileMd5");
            if (StringUtils.hasText(uploadDir) && StringUtils.hasText(fileMd5)) {
                result &= uploadService.deleteChunksDir(uploadDir, fileMd5);
            }
        }
        resp.setSuccess(!result);
        resp.setMessage(!result ? "chunks文件删除成功！" : "文件不存在！");
        return resp;
    }

    @GetMapping("/dirs")
    public CommonResp dir(@RequestParam Map<String, String> req) {
        String dirPath = req.get("dirPath");
        File[] dirs;
        if (StringUtils.hasText(dirPath)) {
            File file = new File(dirPath);
            if (file.isDirectory()) {
                dirs = new File(dirPath).listFiles();
            } else {
                dirs = null;
            }
        } else {
            dirs = File.listRoots();
        }
        List<Map> dirList = new ArrayList<>();
        for (File dir : dirs) {
            if (!dir.isDirectory()) continue;
            boolean isLeaf = true;
            if (dir.list() != null && dir.list().length > 0 && dir.canRead()) {
                isLeaf = false;
            }
            Map<String, Object> dirInfo = new HashMap<>();
            dirInfo.put("path", dir.getPath().replace("\\", "/"));
            dirInfo.put("leaf", isLeaf);
            dirList.add(dirInfo);
        }
        CommonResp<List> resp = new CommonResp<>();
        resp.setSuccess(true);
        resp.setMessage("获取服务器目录成功！");
        resp.setData(dirList);
        return resp;
    }
}
