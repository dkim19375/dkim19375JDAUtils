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

import java.awt.Color
import me.dkim19375.dkim19375jdautils.BotBase
import me.dkim19375.dkim19375jdautils.data.Whitelist
import me.dkim19375.dkim19375jdautils.embed.EmbedUtils
import me.dkim19375.dkim19375jdautils.embed.KotlinEmbedBuilder
import me.dkim19375.dkimcore.annotation.API
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

/**
 * The [Command] class - should be extended in every command class
 *
 * @property bot The [BotBase] of this bot
 * @constructor Creates a [Command] that should be in [BotBase.commands]
 */
@API
@Deprecated("Use slash commands instead")
abstract class Command(private val bot: BotBase) {
    abstract val command: String
    abstract val name: String
    abstract val aliases: Set<String>
    abstract val description: String
    abstract val arguments: Set<CommandArg>
    abstract val type: CommandType
    abstract val minArgs: Int
    open val permissions: Whitelist = Whitelist()
    @Suppress("DEPRECATION")
    open val helpEmbed: (
        user: User,
        guild: Guild?,
        cmd: String,
        command: Command
    ) -> MessageEmbed = { user, guild, cmd, command ->
        KotlinEmbedBuilder.getFirstPreset(
            title = "${bot.name} ${command.name}",
            color = Color.BLUE,
            cmd = cmd,
            user = user
        ).addField(
            "Information:",
            command.description.plus("\n**Prefix: ${bot.getPrefix(guild?.idLong)}**"),
            false
        ).addField(
            EmbedUtils.getEmbedGroup("Aliases:", command.aliases)
        ).addField(
            EmbedUtils.getEmbedGroup("Arguments:", command.arguments.map { arg ->
                "${arg.arg} - ${arg.description}"
            })
        ).build()
    }

    /**
     * Send help usage
     *
     * @param cmd The command of the sent message
     * @param event The [MessageReceivedEvent]
     * @param command the [Command] to set the help message for
     */
    open fun sendHelpUsage(
        cmd: String,
        event: MessageReceivedEvent,
        @Suppress("DEPRECATION") command: Command = this
    ) {
        val guild = if (event.isFromGuild) event.guild else null
        if (!permissions.hasAccess(event.author, event.member, event.channel as? GuildChannel)) {
            return
        }
        event.channel.sendMessageEmbeds(helpEmbed(event.author, guild, cmd, command)).queue()
    }

    /**
     * Called when a message was received
     *
     * Should not be used for general command handling
     *
     * @param message The message that the user sent
     * @param event The [MessageReceivedEvent] of the sent message
     */
    open suspend fun onMessageReceived(
        message: String,
        event: MessageReceivedEvent
    ) {
    }

    /**
     * On command
     *
     * @param cmd The command/alias
     * @param args The args, for example: **!help fun 2** would be **{ "fun", "2" }**
     * @param prefix The prefix of the command sent
     * @param all The entire raw command **excluding** the prefix
     * @param event The [MessageReceivedEvent]
     */
    open suspend fun onCommand(
        cmd: String,
        args: List<String>,
        prefix: String,
        all: String,
        event: MessageReceivedEvent
    ) {
    }
}