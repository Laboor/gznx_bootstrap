package com.gznx.domain;

import javax.validation.constraints.NotNull;
import java.util.Objects;

public class UploadFileInfo {

    private String id;

    private String uid;

    @NotNull(message = "uploadDir不能为空")
    private String uploadDir;

    @NotNull(message = "fileMd5不能为空")
    private String fileMd5;

    @NotNull(message = "fileName不能为空")
    private String fileName;

    private String lastModifiedDate;

    private String size;

    private String chunks;

    private String chunk;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(String lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getChunks() {
        return chunks;
    }

    public void setChunks(String chunks) {
        this.chunks = chunks;
    }

    public String getChunk() {
        return chunk;
    }

    public void setChunk(String chunk) {
        this.chunk = chunk;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UploadFileInfo that = (UploadFileInfo) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(uid, that.uid) &&
                Objects.equals(uploadDir, that.uploadDir) &&
                Objects.equals(fileMd5, that.fileMd5) &&
                Objects.equals(fileName, that.fileName) &&
                Objects.equals(lastModifiedDate, that.lastModifiedDate) &&
                Objects.equals(size, that.size) &&
                Objects.equals(chunks, that.chunks) &&
                Objects.equals(chunk, that.chunk);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, uid, uploadDir, fileMd5, fileName, lastModifiedDate, size, chunks, chunk);
    }

    @Override
    public String toString() {
        return "UploadFileInfo{" +
                "id='" + id + '\'' +
                ", uid='" + uid + '\'' +
                ", uploadDir='" + uploadDir + '\'' +
                ", fileMd5='" + fileMd5 + '\'' +
                ", fileName='" + fileName + '\'' +
                ", lastModifiedDate='" + lastModifiedDate + '\'' +
                ", size='" + size + '\'' +
                ", chunks='" + chunks + '\'' +
                ", chunk='" + chunk + '\'' +
                '}';
    }
}
