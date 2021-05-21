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