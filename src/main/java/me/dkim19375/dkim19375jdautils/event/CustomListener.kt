package me.dkim19375.dkim19375jdautils.event

import me.dkim19375.dkim19375jdautils.annotation.API
import me.dkim19375.dkim19375jdautils.command.Command
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent

abstract class CustomListener {
    @API
    open val antiBot: Boolean = true

    @API
    open fun onMessageReceived(event: MessageReceivedEvent): Boolean = true
    @API
    open fun onGuildMessageReceived(event: GuildMessageReceivedEvent): Boolean = true
    @API
    open fun onPrivateMessageReceived(event: PrivateMessageReceivedEvent): Boolean = true
    @API
    open fun isValid(
        command: Command,
        cmd: String,
        args: List<String>,
        member: Member?,
        user: User,
        guild: Guild?,
        message: Message,
        channel: MessageChannel,
        event: Event
    ): Boolean {
        if (!cmd.equals(command.command, ignoreCase = true)) {
            return false
        }
        if (user.isBot && antiBot) {
            return false
        }
        member?.let {
            if (channel is GuildChannel) {
                if (member.hasPermission(channel, command.permissions)) {
                    return@let
                }
            } else if (member.hasPermission(command.permissions)) {
                return@let
            }
            if (event !is MessageReceivedEvent) {
                channel.sendMessage(
                    "You do not have permission! (Required permission${if (command.permissions.size <= 1) "" else "s"}: ${
                        command.permissions.joinToString(
                            ", ", transform = Permission::getName
                        )
                    })"
                ).queue()
            }
            return false
        }
        if (args.size < command.minArgs) {
            when (event) {
                is PrivateMessageReceivedEvent -> command.sendHelpUsage(cmd, event)
                is GuildMessageReceivedEvent -> command.sendHelpUsage(cmd, event)
            }
            return false
        }
        return true
    }
    open fun isValid(
        command: Command,
        cmd: String,
        args: List<String>,
        event: GuildMessageReceivedEvent
    ): Boolean =
        isValid(command, cmd, args, event.member, event.author, event.guild, event.message, event.channel, event)

    open fun isValid(
        command: Command,
        cmd: String,
        args: List<String>,
        event: MessageReceivedEvent
    ): Boolean = isValid(
        command, cmd, args, event.member, event.author, try {
            event.guild
        } catch (_: IllegalStateException) {
            null
        }, event.message, event.channel, event
    )

    open fun isValid(
        command: Command,
        cmd: String,
        args: List<String>,
        event: PrivateMessageReceivedEvent
    ): Boolean = isValid(command, cmd, args, null, event.author, null, event.message, event.channel, event)
}