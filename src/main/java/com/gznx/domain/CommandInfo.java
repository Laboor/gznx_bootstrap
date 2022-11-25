package com.gznx.domain;

import org.apache.tomcat.jni.Proc;

import javax.validation.constraints.NotNull;
import java.util.Objects;

public class CommandInfo {

    private String id;

    @NotNull(message = "type不能为空")
    private String type;

    @NotNull(message = "commandStr不能为空")
    private String commandStr;

    private String startTime;

    private Thread execThread;

    private Process process;

    private String charset;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCommandStr() {
        return commandStr;
    }

    public void setCommandStr(String commandStr) {
        this.commandStr = commandStr;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public Thread getExecThread() {
        return execThread;
    }

    public void setExecThread(Thread execThread) {
        this.execThread = execThread;
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommandInfo that = (CommandInfo) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getType(), that.getType()) &&
                Objects.equals(getCommandStr(), that.getCommandStr()) &&
                Objects.equals(getStartTime(), that.getStartTime()) &&
                Objects.equals(getExecThread(), that.getExecThread()) &&
                Objects.equals(getProcess(), that.getProcess()) &&
                Objects.equals(getCharset(), that.getCharset());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getType(), getCommandStr(), getStartTime(), getExecThread(), getProcess(), getCharset());
    }

    @Override
    public String toString() {
        return "CommandInfo{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", commandStr='" + commandStr + '\'' +
                ", startTime='" + startTime + '\'' +
                ", execThread=" + execThread +
                ", process=" + process +
                ", charset='" + charset + '\'' +
                '}';
    }
}
