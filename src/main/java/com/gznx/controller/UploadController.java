package com.gznx.controller;

import com.gznx.response.CommonResp;
import com.gznx.service.UploadService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
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
}
