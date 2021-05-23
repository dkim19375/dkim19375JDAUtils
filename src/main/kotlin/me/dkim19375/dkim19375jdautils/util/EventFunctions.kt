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

package me.dkim19375.dkim19375jdautils.util

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.GuildChannel
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.message.GenericMessageEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.GenericGuildMessageEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent
import net.dv8tion.jda.api.events.message.priv.GenericPrivateMessageEvent
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import net.dv8tion.jda.api.events.message.priv.react.PrivateMessageReactionAddEvent
import net.dv8tion.jda.api.events.message.priv.react.PrivateMessageReactionRemoveEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent
import net.dv8tion.jda.api.events.user.GenericUserEvent

fun Event.getUserId(): Long? = when (this) {
    is GuildMessageReceivedEvent -> author.idLong
    is MessageReceivedEvent -> author.idLong
    is PrivateMessageReceivedEvent -> author.idLong
    is GuildMessageReactionAddEvent -> userIdLong
    is MessageReactionAddEvent -> userIdLong
    is PrivateMessageReactionAddEvent -> userIdLong
    is GuildMessageReactionRemoveEvent -> userIdLong
    is MessageReactionRemoveEvent -> userIdLong
    is PrivateMessageReactionRemoveEvent -> userIdLong
    is GenericUserEvent -> user.idLong
    else -> null
}

fun Event.getMessageId(): Long? = when (this) {
    is GenericGuildMessageEvent -> messageIdLong
    is GenericMessageEvent -> messageIdLong
    is GenericPrivateMessageEvent -> messageIdLong
    else -> null
}

fun Member.hasPermission(permissions: Collection<Permission>, channel: GuildChannel? = null): Boolean {
    if (channel != null) {
        return hasPermission(channel, permissions)
    }
    return hasPermission(permissions)
}