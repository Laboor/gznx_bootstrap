package com.gznx.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gznx.response.CommonResp;
import com.gznx.service.UploadService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/file")
public class UploadController {
    @Resource
    private UploadService uploadService;

    @PostMapping("/check")
    public CommonResp fileCheck(@RequestBody Map<String, String> req) {
        String uploadDir = req.get("uploadDir");
        String fileName = req.get("fileName");
        String fileMd5 = req.get("fileMd5");
        CommonResp<String> resp = new CommonResp<>();
        try {
            if (StringUtils.hasText(uploadDir) && StringUtils.hasText(fileName) && StringUtils.hasText(fileMd5)) {
                int result = uploadService.checkBigFile(uploadDir, fileName, fileMd5);
                resp.setData(String.valueOf(result));
                resp.setSuccess(true);
                resp.setMessage("请求成功");
            } else {
                resp.setSuccess(false);
                resp.setMessage("请求缺少必要参数！");
            }
        } catch (Exception e) {
            resp.setSuccess(false);
            resp.setMessage(e.getMessage());
        }
        return resp;
    }

    @PostMapping("/upload")
    public CommonResp fileUpload(@RequestPart(value = "file") MultipartFile uploadFile, @RequestParam Map<String, String> req) {
        String md5 = req.get("md5");
        String chunk = req.get("chunk");
        String uploadDir = req.get("uploadDir");
        CommonResp<String> resp = new CommonResp<>();
        try {
            if (StringUtils.hasText(md5) && StringUtils.hasText(uploadDir)) {
                String filePath = uploadService.upload(uploadDir, uploadFile, md5, chunk);
                resp.setData(filePath);
                resp.setSuccess(true);
                resp.setMessage("上传成功！");
            } else {
                resp.setSuccess(false);
                resp.setMessage("请求缺少必要参数！");
            }
        } catch (Exception e) {
            resp.setSuccess(false);
            resp.setMessage(e.getMessage());
        }
        return resp;
    }

    @PostMapping("/merge")
    public CommonResp fileMerge(@RequestBody Map<String, String> req) {
        String uploadDir = req.get("uploadDir");
        String fileName = req.get("fileName");
        String fileMd5 = req.get("fileMd5");
        CommonResp<String> resp = new CommonResp<>();
        try {
            if (StringUtils.hasText(uploadDir) && StringUtils.hasText(fileName) && StringUtils.hasText(fileMd5)) {
                boolean result = uploadService.fileMeger(uploadDir, fileName, fileMd5);
                resp.setSuccess(result);
                resp.setMessage(result ? "文件合并成功！" : "文件合并失败！");
            } else {
                resp.setSuccess(false);
                resp.setMessage("请求缺少必要参数！");
            }
        } catch (Exception e) {
            resp.setSuccess(false);
            resp.setMessage(e.getMessage());
        }
        return resp;
    }

    @PostMapping("/cancel")
    public CommonResp cancelUpload(@RequestBody Map<String, String> req) {
        String fileListArr = req.get("fileList");
        CommonResp<String> resp = new CommonResp<>();
        try {
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
        } catch (Exception e) {
            resp.setSuccess(false);
            resp.setMessage(e.getMessage());
        }
        return resp;
    }
}
