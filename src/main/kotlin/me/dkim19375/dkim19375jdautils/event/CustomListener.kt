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

import me.dkim19375.dkim19375jdautils.command.Command
import me.dkim19375.dkimcore.annotation.API
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent

/**
 * An abstract class to listen to detect if a command is valid to send
 *
 * For example, permission checking is done here
 */
abstract class CustomListener {
    /**
     * @property antiBot True if the commands should be user-only and not bots
     */
    @API
    open val antiBot: Boolean = true

    /**
     * @property ignoreSelf True if the commands should be ignored if it was ran by this bot
     */
    @API
    open val ignoreSelf: Boolean = true

    /**
     * @param event The [MessageReceivedEvent] of this message
     * @return true if the command should run, false if not
     */
    @API
    open fun onMessageReceived(event: MessageReceivedEvent): Boolean = true

    /**
     * @param event The [GuildMessageReceivedEvent] of this message
     * @return true if the command should run, false if not
     */
    @API
    open fun onGuildMessageReceived(event: GuildMessageReceivedEvent): Boolean = true

    /**
     * @param event The [PrivateMessageReceivedEvent] of this message
     * @return true if the command should run, false if not
     */
    @API
    open fun onPrivateMessageReceived(event: PrivateMessageReceivedEvent): Boolean = true

    /**
     * **NOTE:** This will get called on [MessageReceivedEvent], which will also call [PrivateMessageReceivedEvent] *and*
     * [GuildMessageReceivedEvent]!!
     *
     * @param command the [Command] that was sent
     * @param cmd the exact [command/alias][String] that was sent
     * @param args the [args][List] of the command
     * @param member the [Member] who sent the command in [GuildMessageReceivedEvent], or **null** if the command was
     * sent in [PrivateMessageReceivedEvent] or [MessageReceivedEvent]
     * @param user the [User] who sent the command
     * @param guild the [Guild] of where the command was sent
     * @param message the [Message] that was sent
     * @param channel the [MessageChannel] of where the command was sent
     * @param event either [MessageReceivedEvent], [GuildMessageReceivedEvent], or [PrivateMessageReceivedEvent]
     * @return true if the command should execute
     */
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
        if (user.jda.selfUser.idLong == user.idLong) {
            return false
        }
        member?.let {
            if (command.permissions.hasAccess(user, member, (event as? GuildMessageReceivedEvent)?.channel)) {
                return@let
            }
            if (event !is MessageReceivedEvent) {
                channel.sendMessage("You do not have permission!").queue()
            }
            return false
        }
        if (args.size < command.minArgs) {
            when (event) {
                is PrivateMessageReceivedEvent -> command.sendHelpUsage(cmd, event)
                is GuildMessageReceivedEvent -> command.sendHelpUsage(cmd, event)
                is MessageReceivedEvent -> command.sendHelpUsage(cmd, event)
            }
            return false
        }
        return true
    }
}