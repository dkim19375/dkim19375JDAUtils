package me.dkim19375.dkim19375jdautils.holders;

public class MessageRecievedHolder {
    // (String command, String[] args, String prefix, String all)
    private String command;
    private String[] args;
    private String prefix;
    private String all;

    public MessageRecievedHolder(String command, String[] args, String prefix, String all) {
        this.command = command;
        this.args = args;
        this.prefix = prefix;
        this.all = all;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getAll() {
        return all;
    }

    public void setAll(String all) {
        this.all = all;
    }
}