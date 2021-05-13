package me.dkim19375.dkim19375jdautils.event

import me.dkim19375.dkim19375jdautils.BotBase
import me.dkim19375.dkim19375jdautils.command.Command
import me.dkim19375.dkim19375jdautils.data.MessageReceivedData
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class EventListener(private val bot: BotBase) : ListenerAdapter() {
    private fun getMessage(message: String, serverId: String?): MessageReceivedData? {
        if (serverId == null) {
            if (!message.startsWith(bot.jda.selfUser.asMention.replaceFirst("@".toRegex(), "@!"))) {
                return null
            }
        }
        val prefix: String = if (serverId == null) bot.jda.selfUser.asMention.replaceFirst("@".toRegex(), "@!")
        else if (message.startsWith(bot.jda.selfUser.asMention.replaceFirst("@".toRegex(), "@!"))) {
            bot.jda.selfUser.asMention.replaceFirst("@".toRegex(), "@!")
        } else {
            if (message.startsWith(bot.getPrefix(serverId), ignoreCase = true)) {
                bot.getPrefix(serverId)
            } else {
                return null
            }
        }
        if (message.length <= prefix.length) {
            return null
        }
        var command = message
        command = command.substring(prefix.length)
        command = command.trim { it <= ' ' }
        val allArray = command.split(" ").toTypedArray()
        command = command.split(" ").toTypedArray()[0]
        val argsList = mutableListOf<String>()
        var first = true
        for (s in allArray) {
            if (!first) {
                argsList.add(s)
            }
            first = false
        }
        return MessageReceivedData(command, argsList, prefix, message)
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (!bot.customListener.onMessageReceived(event)) {
            return
        }
        try {
            bot.sendEvent { c -> c.onMessageReceived(event.message.contentRaw, event) }
        } catch (e: Exception) {
            e.printStackTrace()
            event.channel.sendMessage("An internal error has occurred!").queue()
        }
        val msg = getMessage(
            event.message.contentRaw, try {
                event.guild
            } catch (_: IllegalStateException) {
                null
            }?.id
        )
        if (msg != null) {
            bot.sendEvent { c ->
                if (!isValid(c, msg.command, msg.args.toList(), event)) {
                    return@sendEvent
                }
                try {
                    c.onCommand(msg.command, msg.args.toList(), msg.prefix, msg.all, event)
                } catch (e: Exception) {
                    e.printStackTrace()
                    event.channel.sendMessage("An internal error has occurred!").queue()
                }
            }
        }
    }

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        if (!bot.customListener.onGuildMessageReceived(event)) {
            return
        }
        try {
            bot.sendEvent { c -> c.onGuildMessageReceived(event.message.contentRaw, event) }
        } catch (e: Exception) {
            e.printStackTrace()
            event.channel.sendMessage("An internal error has occurred!").queue()
        }
        val msg = getMessage(event.message.contentRaw, event.guild.id)
        if (msg != null) {
            bot.sendEvent { c ->
                if (!isValid(c, msg.command, msg.args.toList(), event)) {
                    return@sendEvent
                }
                try {
                    c.onGuildCommand(msg.command, msg.args.toList(), msg.prefix, msg.all, event)
                } catch (e: Exception) {
                    e.printStackTrace()
                    event.channel.sendMessage("An internal error has occurred!").queue()
                }
            }
        }
    }

    override fun onPrivateMessageReceived(event: PrivateMessageReceivedEvent) {
        if (!bot.customListener.onPrivateMessageReceived(event)) {
            return
        }
        try {
            bot.sendEvent { c -> c.onPrivateMessageReceived(event.message.contentRaw, event) }
        } catch (e: Exception) {
            e.printStackTrace()
            event.channel.sendMessage("An internal error has occurred!").queue()
        }
        val msg = getMessage(event.message.contentRaw, null)
        if (msg != null) {
            try {
                bot.sendEvent { c ->
                    if (!isValid(c, msg.command, msg.args.toList(), event)) {
                        return@sendEvent
                    }
                    try {
                        c.onPrivateCommand(msg.command, msg.args.toList(), msg.prefix, msg.all, event)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        event.channel.sendMessage("An internal error has occurred!").queue()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                event.channel.sendMessage("An internal error has occurred!").queue()
            }
        }
    }

    private fun isValid(
        command: Command,
        cmd: String,
        args: List<String>,
        event: GuildMessageReceivedEvent
    ): Boolean = bot.customListener.isValid(command, cmd, args, event)

    private fun isValid(
        command: Command,
        cmd: String,
        args: List<String>,
        event: MessageReceivedEvent
    ): Boolean =
        bot.customListener.isValid(command, cmd, args, event)

    private fun isValid(
        command: Command,
        cmd: String,
        args: List<String>,
        event: PrivateMessageReceivedEvent
    ): Boolean = bot.customListener.isValid(command, cmd, args, event)
}