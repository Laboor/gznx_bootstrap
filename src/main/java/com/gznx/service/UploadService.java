package com.gznx.service;

import com.gznx.websocket.Constant;
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

    private String formatUploadPath(String uploadDir) {
        if (File.separator.equals(uploadDir.substring(uploadDir.length() - 1))) {
            return uploadDir;
        }
        return uploadDir + File.separator;
    }

    private boolean hasPermission(String filePath) {
        File file = new File(filePath);
        return file.canRead() && file.canWrite();
    }

    public int checkBigFile(String uploadDir, String fileMd5) throws Exception {
        uploadDir = formatUploadPath(uploadDir);
        if (!hasPermission(uploadDir)) {
            throw new Exception("没有对该文件或文件夹的读写权限！");
        }
        File mergeMd5Dir = new File(uploadDir + Constant.UPLOAD_MERGE_TEMP_DIR + fileMd5);
        if (mergeMd5Dir.exists()) {
            mergeMd5Dir.mkdirs();
            return -1; //文件已存在，下标为-1
        }
        // 读取目录里的所有文件
        File dir = new File(uploadDir + fileMd5);
        File[] childs = dir.listFiles();
        // 文件未上传过为0，上传中断过返回当前下标
        return childs == null ? 0 : childs.length - 1;
    }

    public String upload(String uploadDir, MultipartFile multipartFile, String fileMd5, String chunkIndex) throws Exception {
        uploadDir = formatUploadPath(uploadDir);
        if (!hasPermission(uploadDir)) {
            throw new Exception("没有对该文件或文件夹的读写权限！");
        }
        int fileNamelength = multipartFile.getOriginalFilename().length();
        if (fileNamelength > Constant.DEFAULT_FILE_NAME_LENGTH) {
            throw new Exception("[" + fileNamelength + "]文件名超过最大长度：" + Constant.DEFAULT_FILE_NAME_LENGTH);
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
        try {
            if (!hasPermission(uploadDir)) {
                throw new Exception("没有对该文件或文件夹的读写权限！");
            }
            // 读取目录里的所有文件
            File dir = getAbsoluteFile(uploadDir, fileMd5);
            File[] childs = dir.listFiles();
            if (Objects.isNull(childs) || childs.length == 0) {
                return false;
            }
            // 转成集合，便于排序
            List<File> fileList = new ArrayList<File>(Arrays.asList(childs));
            Collections.sort(fileList, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    if (Integer.parseInt(o1.getName()) < Integer.parseInt(o2.getName())) {
                        return -1;
                    }
                    return 1;
                }
            });
            // 合并后的文件
            File outputFile = getAbsoluteFile(uploadDir + Constant.UPLOAD_MERGE_TEMP_DIR + fileMd5, fileName);
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
                //发生异常，文件合并失败 ，删除创建的文件
                LOG.error("发生异常，文件合并失败 ，删除创建的文件！", e);
                outputFile.delete();
                dir.delete();//删除文件夹
                throw e;
            } finally {
                if (inChannel != null) {
                    inChannel.close();
                }
            }
            dir.delete(); //删除分片所在的文件夹
        } catch (IOException e) {
            LOG.error("合并文件发生异常！", e);
            throw e;
        } finally {
            try {
                if (outChannel != null) {
                    outChannel.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
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
