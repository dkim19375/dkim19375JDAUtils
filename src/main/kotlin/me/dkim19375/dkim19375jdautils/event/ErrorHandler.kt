package me.dkim19375.dkim19375jdautils.event

import me.dkim19375.dkim19375jdautils.command.Command
import me.dkim19375.dkim19375jdautils.data.MessageReceivedData
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent

open class ErrorHandler {

    open fun onMessageReceived(error: Throwable, event: MessageReceivedEvent, command: Command) {
        error.printStackTrace()
        event.channel.sendMessage("An internal error has occurred!").queue()
    }

    open fun onMessageReceivedCommand(
        error: Throwable,
        event: MessageReceivedEvent,
        command: Command,
        data: MessageReceivedData
    ) {
        error.printStackTrace()
        event.channel.sendMessage("An internal error has occurred!").queue()
    }


    open fun onGuildMessageReceived(error: Throwable, event: GuildMessageReceivedEvent, command: Command) {
        error.printStackTrace()
        event.channel.sendMessage("An internal error has occurred!").queue()
    }

    open fun onGuildMessageReceivedCommand(
        error: Throwable,
        event: GuildMessageReceivedEvent,
        command: Command,
        data: MessageReceivedData
    ) {
        error.printStackTrace()
        event.channel.sendMessage("An internal error has occurred!").queue()
    }


    open fun onPrivateMessageReceived(error: Throwable, event: PrivateMessageReceivedEvent, command: Command) {
        error.printStackTrace()
        event.channel.sendMessage("An internal error has occurred!").queue()
    }

    open fun onPrivateMessageReceivedCommand(
        error: Throwable,
        event: PrivateMessageReceivedEvent,
        command: Command,
        data: MessageReceivedData
    ) {
        error.printStackTrace()
        event.channel.sendMessage("An internal error has occurred!").queue()
    }

}