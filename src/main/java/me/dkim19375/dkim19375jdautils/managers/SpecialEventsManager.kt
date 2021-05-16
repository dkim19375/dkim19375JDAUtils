package me.dkim19375.dkim19375jdautils.managers

import me.dkim19375.dkim19375jdautils.BotBase
import me.dkim19375.dkim19375jdautils.annotation.API
import me.dkim19375.dkim19375jdautils.util.EventType
import me.dkim19375.dkim19375jdautils.util.getMessageId
import me.dkim19375.dkim19375jdautils.util.getUserId
import me.dkim19375.dkim19375jdautils.util.hasPermission
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.events.message.priv.react.PrivateMessageReactionAddEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.RestAction
import kotlin.reflect.KClass

open class SpecialEventsManager(private val bot: BotBase) : ListenerAdapter() {
    private val events = mutableListOf<(Event) -> Unit>()
    private val singleEvents = mutableMapOf<Type, MutableList<(Event) -> Unit>>()

    override fun onGuildMessageReactionAdd(event: GuildMessageReactionAddEvent) = onEvent(Type.REACTION_ADD, event)
    override fun onPrivateMessageReactionAdd(event: PrivateMessageReactionAddEvent) = onEvent(Type.REACTION_ADD, event)
    override fun onMessageReactionAdd(event: MessageReactionAddEvent) = onEvent(Type.REACTION_ADD, event)

    private fun onEvent(@Suppress("SameParameterValue") type: Type, event: Event) {
        events.forEach { it(event) }
        for ((otherType, list) in singleEvents.toMap()) {
            if (otherType != type) {
                continue
            }
            singleEvents.remove(otherType)
            list.forEach { it(event) }
        }
    }

    @API
    fun onReactionAdd(
        permanent: Boolean,
        eventType: EventType,
        action: (Event, Guild?, MessageReaction.ReactionEmote, MessageChannel, User, Message, Member?) -> Unit,
        requiredMessage: Long = 0,
        requiredChannel: Long = 0,
        requiredGuild: Long = 0,
        whitelist: Set<Long>? = null,
        requiredPerms: Set<Permission> = emptySet(),
        removeIfNoPerms: Boolean = false,
        reaction: MessageReaction.ReactionEmote? = null,
    ) {
        val actionVar: (Event) -> Unit = action@{ event ->
            val jda = event.jda
            when (event) {
                is GuildMessageReactionAddEvent -> if (eventType != EventType.GUILD) return@action
                is MessageReactionAddEvent -> if (eventType != EventType.GENERIC) return@action
                is PrivateMessageReactionAddEvent -> if (eventType != EventType.PRIVATE) return@action
            }
            val messageId: Long = event.getMessageId() ?: return@action
            val userId: Long = event.getUserId() ?: return@action
            val guild: Guild? = when (event) {
                is GuildMessageReactionAddEvent -> event.guild
                else -> null
            }
            val emoji: MessageReaction.ReactionEmote = when (event) {
                is GuildMessageReactionAddEvent -> event.reactionEmote
                is MessageReactionAddEvent -> event.reactionEmote
                is PrivateMessageReactionAddEvent -> event.reactionEmote
                else -> return@action
            }
            val channel = when (event) {
                is GuildMessageReactionAddEvent -> event.channel
                is MessageReactionAddEvent -> event.channel
                is PrivateMessageReactionAddEvent -> event.channel
                else -> return@action
            }
            if (requiredChannel != 0L && requiredChannel != channel.idLong) {
                return@action
            }
            if (reaction != null) {
                if (reaction.name != emoji.name) {
                    return@action
                }
            }
            val retrievedMessage: RestAction<Message> = when (event) {
                is GuildMessageReactionAddEvent -> event.retrieveMessage()
                is MessageReactionAddEvent -> event.retrieveMessage()
                is PrivateMessageReactionAddEvent -> event.channel.retrieveMessageById(event.messageIdLong)
                else -> return@action
            }
            if (requiredGuild != guild?.idLong && (requiredGuild != 0L && guild != null)) {
                return@action
            }
            if (requiredMessage != messageId && requiredMessage != 0L) {
                return@action
            }
            retrievedMessage.queue message@{ msg ->
                jda.retrieveUserById(userId).queue userQ@{ user ->
                    if (whitelist?.contains(userId) != false) {
                        if (!removeIfNoPerms) {
                            return@userQ
                        }
                        when {
                            emoji.isEmoji -> msg.removeReaction(emoji.emoji, user)
                            emoji.isEmote -> msg.removeReaction(emoji.emote, user)
                        }
                        return@userQ
                    }
                    guild?.retrieveMemberById(userId)?.queue memberQueue@{ member ->
                        if (!member.hasPermission(requiredPerms, channel as? GuildChannel)) {
                            return@memberQueue
                        }
                        action(event, guild, emoji, channel, user, msg, member)
                    } ?: action(event, guild, emoji, channel, user, msg, null)
                }
            }
        }
        if (permanent) {
            events.add(actionVar)
            return
        }
        singleEvents.getOrPut(Type.REACTION_ADD) { mutableListOf() }.add(actionVar)
    }

    enum class Type(@API val classes: Set<KClass<out Event>>) {
        REACTION_ADD(
            setOf(
                MessageReactionAddEvent::class,
                GuildMessageReactionAddEvent::class,
                PrivateMessageReactionAddEvent::class
            )
        )
    }
}