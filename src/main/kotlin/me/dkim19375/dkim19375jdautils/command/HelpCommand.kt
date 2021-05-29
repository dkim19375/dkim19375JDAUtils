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

package me.dkim19375.dkim19375jdautils.command

import me.dkim19375.dkim19375jdautils.BotBase
import me.dkim19375.dkim19375jdautils.annotation.API
import me.dkim19375.dkim19375jdautils.embed.EmbedUtils
import me.dkim19375.dkim19375jdautils.embed.KotlinEmbedBuilder
import me.dkim19375.dkim19375jdautils.util.getCommand
import me.dkim19375.dkim19375jdautils.util.getOfType
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import java.awt.Color

val OTHER_TYPE: CommandType = object : CommandType("OTHER", "Other") {}

/**
 * A default (optional) help command.
 *
 * In order to use it, you **must** register it in [BotBase.commands]!
 *
 * @property bot the [BotBase] of the bot
 * @constructor Creates a help command
 */
@API
open class HelpCommand(private val bot: BotBase) : Command(bot) {
    override val command = "help"
    override val name = "Help"
    override val aliases = setOf<String>()
    override val description = "See the bot's commands"
    override val arguments: Set<CommandArg>
        get() = bot.getAllCommandTypes().map { type ->
            CommandArg(
                this,
                type.displayname.lowercase(),
                "View commands in the ${type.displayname.lowercase()} category"
            )
        }.toSet()
    override val type = OTHER_TYPE
    override val minArgs = 1
    open val embed: (MessageReceivedEvent, String) -> MessageEmbed = { event, cmd ->
        KotlinEmbedBuilder.getFirstPreset(
            title = "${bot.name} $name: ${type.displayname}",
            color = Color.BLUE,
            cmd = cmd,
            user = event.author
        ).addField(
            "TIP:", "Do ${bot.getPrefix(event.guild.id)}help <command> " +
                    "to view information about a specific command!", false
        ).addField(
            "Information:",
            "Commands in the ${type.displayname} category",
            false
        ).addField(
            EmbedUtils.getEmbedGroup("Commands - ${type.displayname}:", bot.commands.getOfType(type).map { c ->
                "${c.command} - ${c.description}"
            })
        ).build()
    }

    /**
     * On help command
     *
     * @param cmd The command/alias
     * @param args The args, for example: **!help fun 2** would be **{ "fun", "2" }**
     * @param prefix The prefix of the command sent
     * @param all The entire raw command **excluding** the prefix
     * @param event The [GuildMessageReceivedEvent]
     */
    override suspend fun onCommand(
        cmd: String,
        args: List<String>,
        prefix: String,
        all: String,
        event: MessageReceivedEvent
    ) {
        val type = args[0].getCommandType(bot)
        if (type == null) {
            val command = args[0].getCommand(bot)
            if (command == null) {
                sendHelpUsage(cmd, event)
                return
            }
            sendHelpUsage(cmd, event, command)
            return
        }
        event.channel.sendMessage(embed(event, cmd)).queue()
    }
}