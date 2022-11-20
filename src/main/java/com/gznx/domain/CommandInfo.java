package com.gznx.domain;

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
        return Objects.equals(id, that.id) &&
                Objects.equals(type, that.type) &&
                Objects.equals(commandStr, that.commandStr) &&
                Objects.equals(startTime, that.startTime) &&
                Objects.equals(execThread, that.execThread) &&
                Objects.equals(charset, that.charset);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, commandStr, startTime, execThread, charset);
    }

    @Override
    public String toString() {
        return "CommandInfo{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", commandStr='" + commandStr + '\'' +
                ", startTime='" + startTime + '\'' +
                ", execThread=" + execThread +
                ", charset='" + charset + '\'' +
                '}';
    }
}
