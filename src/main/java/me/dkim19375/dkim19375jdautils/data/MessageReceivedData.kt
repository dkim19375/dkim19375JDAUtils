package me.dkim19375.dkim19375jdautils.holders;

import org.jetbrains.annotations.NotNull;

public class MessageReceivedHolder {
    // (String command, String[] args, String prefix, String all)
    @NotNull
    private final String command;
    @NotNull
    private final String[] args;
    @NotNull
    private final String prefix;
    @NotNull
    private final String all;

    public MessageReceivedHolder(@NotNull String command, @NotNull String[] args, @NotNull String prefix, @NotNull String all) {
        this.command = command;
        this.args = args;
        this.prefix = prefix;
        this.all = all;
    }

    @NotNull
    public String getCommand() {
        return command;
    }

    @NotNull
    public String[] getArgs() {
        return args;
    }

    @NotNull
    public String getPrefix() {
        return prefix;
    }

    @NotNull
    public String getAll() {
        return all;
    }
}