package com.gznx.domain;

import com.gznx.utils.Md5Utils;

import javax.validation.constraints.NotNull;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

public class CommandInfo {
    public CommandInfo(String type, String commandStr) {
        this.id = Md5Utils.hash(type + commandStr);
        this.type = type;
        this.commandStr = commandStr;
        this.commandArgs = commandStr.replace("\\", "/").split("\\s+");
    }

    private String id;

    @NotNull(message = "type不能为空")
    private String type;

    @NotNull(message = "commandStr不能为空")
    private String commandStr;

    private String[] commandArgs;

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
        this.commandArgs = commandStr.replace("\\", "/").split("\\s+");
    }

    public String[] getCommandArgs() {
        return commandArgs;
    }

    public void setCommandArgs(String[] commandArgs) {
        this.commandArgs = commandArgs;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        this.startTime = dateFormat.format(startTime);
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
                Arrays.equals(commandArgs, that.commandArgs) &&
                Objects.equals(startTime, that.startTime) &&
                Objects.equals(execThread, that.execThread) &&
                Objects.equals(charset, that.charset);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, type, commandStr, startTime, execThread, charset);
        result = 31 * result + Arrays.hashCode(commandArgs);
        return result;
    }
}
