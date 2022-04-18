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

import kotlinx.coroutines.launch
import me.dkim19375.dkim19375jdautils.BotBase
import me.dkim19375.dkim19375jdautils.command.Command
import me.dkim19375.dkim19375jdautils.data.MessageReceivedData
import me.dkim19375.dkimcore.extension.SCOPE
import me.dkim19375.dkimcore.extension.typedNull
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class EventListener(private val bot: BotBase) : ListenerAdapter() {
    private fun getMessage(message: String, serverId: String?, guild: Boolean): MessageReceivedData? {
        val prefix: String
        if (guild) {
            val mention = bot.jda.selfUser.asMention
            val replacedMention = mention.replaceFirst("@", "@!")
            val removedMention = mention.replaceFirst("@!", "@")
            if (serverId == null) {
                if (!message.startsWith(mention.replaceFirst("@", "@!"))) {
                    return null
                }
            }
            prefix = when {
                serverId == null -> removedMention
                message.startsWith(replacedMention) -> replacedMention
                message.startsWith(removedMention) -> removedMention
                message.startsWith(bot.getPrefix(serverId), ignoreCase = true) -> bot.getPrefix(serverId)
                else -> return null
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

    @Suppress("DEPRECATION")
    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (!bot.customListener.onMessageReceived(event)) {
            return
        }
        bot.sendEvent { command ->
            SCOPE.launch {
                try {
                    command.onMessageReceived(event.message.contentRaw, event)
                } catch (e: Throwable) {
                    bot.errorHandler.onMessageReceived(e, event, command)
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
            bot.sendEvent { command ->
                SCOPE.launch {
                    if (!isValid(command, msg.command, msg.args.toList(), event)) {
                        return@launch
                    }
                    try {
                        command.onCommand(msg.command, msg.args.toList(), msg.prefix, msg.all, event)
                    } catch (e: Exception) {
                        bot.errorHandler.onMessageReceivedCommand(e, event, command, msg)
                    }
                }
            }
        }
    }

    private fun isValid(
        command: Command,
        cmd: String,
        args: List<String>,
        event: MessageReceivedEvent
    ): Boolean = bot.customListener.isValid(
        command = command,
        cmd = cmd,
        args = args,
        member = event.member,
        user = event.author,
        guild = try {
            event.guild
        } catch (_: IllegalStateException) {
            null
        },
        message = event.message,
        channel = event.channel,
        event = event
    )
}