/*
 * MIT License
 *
 * Copyright (c) 2021 dkim19375
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.dkim19375.dkim19375jdautils.event

import kotlinx.coroutines.runBlocking
import me.dkim19375.dkim19375jdautils.BotBase
import me.dkim19375.dkim19375jdautils.command.Command
import me.dkim19375.dkim19375jdautils.data.MessageReceivedData
import me.dkim19375.dkim19375jdautils.util.typedNull
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class EventListener(private val bot: BotBase) : ListenerAdapter() {
    private fun getMessage(message: String, serverId: String?, guild: Boolean): MessageReceivedData? {
        val prefix: String
        if (guild) {
            if (serverId == null) {
                if (!message.startsWith(bot.jda.selfUser.asMention.replaceFirst("@".toRegex(), "@!"))) {
                    return null
                }
            }
            prefix = if (serverId == null) bot.jda.selfUser.asMention.replaceFirst("@".toRegex(), "@!")
            else if (message.startsWith(bot.jda.selfUser.asMention.replaceFirst("@".toRegex(), "@!"))) {
                bot.jda.selfUser.asMention.replaceFirst("@".toRegex(), "@!")
            } else {
                if (message.startsWith(bot.getPrefix(serverId), ignoreCase = true)) {
                    bot.getPrefix(serverId)
                } else {
                    return null
                }
            }
        } else {
            prefix = bot.getPrefix(typedNull<Long>())
        }
        if (message.length <= prefix.length) {
            return null
        }
        var command = message
        command = command.removePrefix(prefix)
        command = command.trim()
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
        bot.sendEvent { c ->
            runBlocking {
                try {
                    c.onMessageReceived(event.message.contentRaw, event)
                } catch (e: Exception) {
                    e.printStackTrace()
                    event.channel.sendMessage("An internal error has occurred!").queue()
                }
            }
        }
        val msg = getMessage(
            event.message.contentRaw, try {
                event.guild
            } catch (_: IllegalStateException) {
                null
            }?.id,
            event.isFromGuild
        )
        if (msg != null) {
            bot.sendEvent { c ->
                runBlocking {
                    if (!isValid(c, msg.command, msg.args.toList(), event)) {
                        return@runBlocking
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
    }

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        if (!bot.customListener.onGuildMessageReceived(event)) {
            return
        }
        bot.sendEvent { c ->
            runBlocking {
                try {
                    c.onGuildMessageReceived(event.message.contentRaw, event)

                } catch (e: Exception) {
                    e.printStackTrace()
                    event.channel.sendMessage("An internal error has occurred!").queue()
                }
            }
        }
        val msg = getMessage(event.message.contentRaw, event.guild.id, true)
        if (msg != null) {
            bot.sendEvent { c ->
                runBlocking {
                    if (!isValid(c, msg.command, msg.args.toList(), event)) {
                        return@runBlocking
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
    }

    override fun onPrivateMessageReceived(event: PrivateMessageReceivedEvent) {
        if (!bot.customListener.onPrivateMessageReceived(event)) {
            return
        }
        bot.sendEvent { c ->
            runBlocking {
                try {
                    c.onPrivateMessageReceived(event.message.contentRaw, event)
                } catch (e: Exception) {
                    e.printStackTrace()
                    event.channel.sendMessage("An internal error has occurred!").queue()
                }
            }
        }
        val msg = getMessage(event.message.contentRaw, null, false)
        if (msg != null) {
            try {
                bot.sendEvent { c ->
                    runBlocking {
                        if (!isValid(c, msg.command, msg.args.toList(), event)) {
                            return@runBlocking
                        }
                        try {
                            c.onPrivateCommand(msg.command, msg.args.toList(), msg.prefix, msg.all, event)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            event.channel.sendMessage("An internal error has occurred!").queue()
                        }
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
    ): Boolean = bot.customListener.isValid(
        command,
        cmd,
        args,
        event.member,
        event.author,
        event.guild,
        event.message,
        event.channel,
        event
    )

    private fun isValid(
        command: Command,
        cmd: String,
        args: List<String>,
        event: MessageReceivedEvent
    ): Boolean = bot.customListener.isValid(
        command, cmd, args, event.member, event.author, try {
            event.guild
        } catch (_: IllegalStateException) {
            null
        }, event.message, event.channel, event
    )

    private fun isValid(
        command: Command,
        cmd: String,
        args: List<String>,
        event: PrivateMessageReceivedEvent
    ): Boolean = bot.customListener.isValid(
        command,
        cmd,
        args,
        null,
        event.author,
        null,
        event.message,
        event.channel,
        event
    )
}