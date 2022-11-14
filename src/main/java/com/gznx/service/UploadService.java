package com.gznx.service;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.*;

@Service
public class UploadService {
    private static final Logger LOG = LoggerFactory.getLogger(UploadService.class);
    public static final int DEFAULT_FILE_NAME_LENGTH = 100; //文件名最长长度

    private String formatUploadPath(String uploadDir) {
        if (File.separator.equals(uploadDir.substring(uploadDir.length() - 1))) {
            return uploadDir;
        }
        return uploadDir + File.separator;
    }

    public int checkBigFile(String uploadDir, String fileName, String fileMd5) {
        uploadDir = formatUploadPath(uploadDir);
//        // 不覆盖原文件
//        File file = new File(uploadDir + fileName);
//        if (file.exists()) {
//            return -1; //文件已存在，下标为-1
//        }
        // 读取目录里的所有文件
        File fileMd5Dir = new File(uploadDir + fileMd5);
        File[] chunks = fileMd5Dir.listFiles();
        // 文件未上传过为0，上传中断过返回当前下标
        return chunks == null ? 0 : chunks.length - 1;
    }

    public String upload(String uploadDir, MultipartFile multipartFile, String fileMd5, String chunkIndex) throws Exception {
        uploadDir = formatUploadPath(uploadDir);
        int fileNamelength = multipartFile.getOriginalFilename().length();
        if (fileNamelength > DEFAULT_FILE_NAME_LENGTH) {
            throw new Exception("[" + fileNamelength + "]文件名超过最大长度：" + DEFAULT_FILE_NAME_LENGTH);
        }
        File fileChunk;
        if (StringUtils.hasText(chunkIndex)) {
            fileChunk = getAbsoluteFile(uploadDir + fileMd5, chunkIndex);
            FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), fileChunk);
        } else {
            fileChunk = getAbsoluteFile(uploadDir, multipartFile.getOriginalFilename());
            multipartFile.transferTo(fileChunk);
        }
        return fileChunk.getAbsolutePath();
    }

    public boolean fileMeger(String uploadDir, String fileName, String fileMd5) throws Exception {
        uploadDir = formatUploadPath(uploadDir);
        FileChannel outChannel = null;
        File chunksDir = null;
        try {
            // 读取目录里的所有文件
            chunksDir = new File(uploadDir + fileMd5);
            File[] chunks = chunksDir.listFiles();
            if (Objects.isNull(chunks) || chunks.length == 0) {
                return false;
            }
            // 转成集合，便于排序
            List<File> fileList = new ArrayList<File>(Arrays.asList(chunks));
            Collections.sort(fileList, new Comparator<File>() {
                @Override
                public int compare(File file1, File file2) {
                    if (Integer.parseInt(file1.getName()) < Integer.parseInt(file2.getName())) {
                        return -1;
                    }
                    return 1;
                }
            });
            // 合并文件
            File outputFile = getAbsoluteFile(uploadDir, fileName);
            outChannel = new FileOutputStream(outputFile).getChannel();
            FileChannel inChannel = null;
            try {
                for (File file : fileList) {
                    inChannel = new FileInputStream(file).getChannel();
                    inChannel.transferTo(0, inChannel.size(), outChannel);
                    inChannel.close();
                    // 删除分片
                    file.delete();
                }
            } catch (Exception e) {
                LOG.error("发生异常，文件合并失败 ，删除创建的文件！", e);
                outputFile.delete(); // 删除目标文件
                chunksDir.delete();  // 删除chunks文件夹
                throw e;
            } finally {
                if (inChannel != null) {
                    inChannel.close();
                }
            }
        } catch (IOException e) {
            LOG.error("创建目标文件异常！", e);
            throw e;
        } finally {
            try {
                if (outChannel != null) {
                    outChannel.close();
                }
                if (chunksDir != null && chunksDir.exists()) {
                    chunksDir.delete(); // 删除分片所在的文件夹
                }
            } catch (IOException e) {
                LOG.error("关闭输出流异常或删除chunks文件夹异常！", e);
                throw e;
            }
        }
        return true;
    }

    private File getAbsoluteFile(String uploadDir, String fileName) throws IOException {
        uploadDir = formatUploadPath(uploadDir);
        File desc = new File(uploadDir + fileName);

        if (!desc.getParentFile().exists()) {
            desc.getParentFile().mkdirs();
        }
        if (!desc.exists()) {
            desc.createNewFile();
        }
        return desc;
    }
}
