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

package me.dkim19375.dkim19375jdautils.manager

import kotlinx.coroutines.runBlocking
import me.dkim19375.dkim19375jdautils.BotBase
import me.dkim19375.dkim19375jdautils.annotation.API
import me.dkim19375.dkim19375jdautils.data.Whitelist
import me.dkim19375.dkim19375jdautils.util.*
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.events.message.priv.react.PrivateMessageReactionAddEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.util.*
import kotlin.reflect.KClass

/**
 * Special events manager
 *
 * A manager to listen for specific events and create helpful methods to
 * help listen to these events
 *
 * @property bot The [BotBase] of the bot
 */
open class SpecialEventsManager(private val bot: BotBase) : ListenerAdapter() {
    @API
    val events = mutableMapOf<UUID, (Event) -> ActionConsumer<Pair<Boolean, Boolean>>>()

    @API
    val singleEvents = mutableMapOf<Type, MutableMap<UUID, (Event) -> ActionConsumer<Pair<Boolean, Boolean>>>>()

    override fun onGuildMessageReactionAdd(event: GuildMessageReactionAddEvent) =
        runBlocking { onEvent(Type.REACTION_ADD, event) }

    override fun onPrivateMessageReactionAdd(event: PrivateMessageReactionAddEvent) =
        runBlocking { onEvent(Type.REACTION_ADD, event) }

    override fun onMessageReactionAdd(event: MessageReactionAddEvent) =
        runBlocking { onEvent(Type.REACTION_ADD, event) }

    @API
    fun getTask(uuid: UUID): ((Event) -> ActionConsumer<Pair<Boolean, Boolean>>)? {
        events[uuid]?.let {
            return it
        }
        for (map in singleEvents.values) {
            for ((newUUID, event) in map) {
                if (newUUID == uuid) {
                    return event
                }
            }
        }
        return null
    }

    @API
    @Synchronized
    fun removeTask(uuid: UUID) {
        events.remove(uuid)
        singleEvents.forEach { (_, map) -> map.remove(uuid) }
    }

    @Synchronized
    protected open suspend fun onEvent(@Suppress("SameParameterValue") type: Type, event: Event) {
        events.toList().forEach { e ->
            val result = e.second(event).await()
            if (result.second && result.first) {
                events.removeIf { uuid, _ -> uuid == e.first }
            }
        }
        for ((otherType, list) in singleEvents.toMap()) {
            if (otherType != type) {
                continue
            }
            for (expression in list.toList()) {
                val result = expression.second(event).await()
                if (result.first) {
                    list.removeIf { uuid, _ -> uuid == expression.first }
                }
            }
            if (singleEvents.getOrDefault(otherType, mutableMapOf()).isEmpty()) {
                singleEvents.remove(otherType)
            }
        }
    }

    /**
     * Called when a user adds a reaction, based off of [MessageReactionAddEvent]
     *
     * @param permanent True if the event should be called permanently,
     * false if should be no longer called again once called
     * @param eventType The [EventType] of when this event should be called
     * @param action The Action of what should happen on event.
     *
     * Returns **true** if the event should keep occurring, **false** if the event should stop occurring.
     * Does not matter if [permanent] is false
     * @param requiredMessage The required [Message] ID of the reacted message, 0 if it can apply to any message
     * @param requiredChannel The required [Channel][MessageChannel] ID of the reacted message,
     * 0 if it can apply to any channel
     * @param requiredGuild The required [Guild] ID of the reacted message, 0 if it can apply to any guild
     * @param whitelist The [Whitelist] for the user adding the reaction
     * @param removeIfNoPerms True if the reaction should be removed if a [User] doesn't have permissions.
     * **DOES NOT APPLY TO BOTS OR SELF**
     * @param removeBotIfNoPerms True if the reaction should be removed it a [Bot][User] doesn't have permissions.
     * **DOES NOT APPLY TO NON-BOTS**
     * @param removeSelfIfNoPerms True if the reaction should be removed it this bot, [SelfUser] doesn't have permissions.
     * **DOES NOT APPLY TO ANY USER BESIDES THIS**
     * @param reaction The reaction that the event should only apply to, null if it can apply to any reaction
     * @param debug True if it should print debug messages, false if not
     */
    @API
    open fun onReactionAdd(
        permanent: Boolean,
        eventType: EventType,
        action: (Event, Guild?, MessageReaction.ReactionEmote, MessageChannel, User, Message, Member?, UUID) -> Boolean,
        requiredMessage: Long = 0,
        requiredChannel: Long = 0,
        requiredGuild: Long = 0,
        whitelist: Whitelist = Whitelist(),
        removeIfNoPerms: Boolean = false,
        removeBotIfNoPerms: Boolean = false,
        removeSelfIfNoPerms: Boolean = false,
        reaction: MessageReaction.ReactionEmote? = null,
        debug: Boolean = false
    ): UUID {
        val combined = events.keys.plus(singleEvents.values.map { a -> a.keys }.combine())
        val uuid = combined.getRandomUUID()
        val actionVar: (Event) -> ActionConsumer<Pair<Boolean, Boolean>> = actionLabel@{ event ->
            return@actionLabel ActionConsumer {
                runBlocking {
                    onReactionAddCoroutine(
                        permanent,
                        eventType,
                        action,
                        requiredMessage,
                        requiredChannel,
                        requiredGuild,
                        whitelist,
                        removeIfNoPerms,
                        removeBotIfNoPerms,
                        removeSelfIfNoPerms,
                        reaction,
                        debug,
                        uuid,
                        event
                    )
                }
            }
        }
        if (debug) {
            println("added to variables")
        }
        if (permanent) {
            events[uuid] = actionVar
            return uuid
        }
        singleEvents.getOrPut(Type.REACTION_ADD) { mutableMapOf() }[uuid] = actionVar
        return uuid
    }

    protected open suspend fun onReactionAddCoroutine(
        permanent: Boolean,
        eventType: EventType,
        action: (Event, Guild?, MessageReaction.ReactionEmote, MessageChannel, User, Message, Member?, UUID) -> Boolean,
        requiredMessage: Long = 0,
        requiredChannel: Long = 0,
        requiredGuild: Long = 0,
        whitelist: Whitelist = Whitelist(),
        removeIfNoPerms: Boolean = false,
        removeBotIfNoPerms: Boolean = false,
        removeSelfIfNoPerms: Boolean = false,
        reaction: MessageReaction.ReactionEmote? = null,
        debug: Boolean = false,
        uuid: UUID,
        event: Event
    ): Pair<Boolean, Boolean> {
        if (debug) {
            println("called ------------")
        }
        val jda = event.jda
        when (event) {
            is GuildMessageReactionAddEvent -> if (eventType != EventType.GUILD) {
                return Pair(first = false, second = false)
            }
            is MessageReactionAddEvent -> if (eventType != EventType.GENERIC) {
                return Pair(first = false, second = false)
            }
            is PrivateMessageReactionAddEvent -> if (eventType != EventType.PRIVATE) {
                return Pair(first = false, second = false)
            }
        }
        if (debug) {
            println("passed EventType test")
        }
        val messageId: Long = event.getMessageId() ?: let {
            return Pair(first = false, second = false)
        }
        if (debug) {
            println("passed messageId 1")
        }
        val userId: Long = event.getUserId() ?: let {
            return Pair(first = false, second = false)
        }
        if (debug) {
            println("passed userId test")
        }
        val emoji: MessageReaction.ReactionEmote = when (event) {
            is GuildMessageReactionAddEvent -> event.reactionEmote
            is MessageReactionAddEvent -> event.reactionEmote
            is PrivateMessageReactionAddEvent -> event.reactionEmote
            else -> {
                return Pair(first = false, second = false)
            }
        }
        if (debug) {
            println("passed emoji test")
        }
        val channel = when (event) {
            is GuildMessageReactionAddEvent -> event.channel
            is MessageReactionAddEvent -> event.channel
            is PrivateMessageReactionAddEvent -> event.channel
            else -> {
                return Pair(first = false, second = false)
            }
        }
        if (debug) {
            println("passed channel test")
        }
        val guild: Guild? = (channel as? GuildChannel)?.guild
        if (requiredChannel != 0L && requiredChannel != channel.idLong) {
            if (debug) {
                println("stopped - requiredChannel: $requiredChannel, channel: ${channel.idLong}")
            }
            return Pair(first = false, second = false)
        }
        if (debug) {
            println("passed channel id test")
        }
        if (reaction != null) {
            if (reaction.name != emoji.name) {
                if (debug) {
                    println("stopped - reaction name: ${reaction.name}, emoji name: ${emoji.name}")
                }
                return Pair(first = false, second = false)
            }
        }
        if (debug) {
            println("passed reaction tests")
        }
        val msg: Message = when (event) {
            is GuildMessageReactionAddEvent -> event.retrieveMessage().await()
            is MessageReactionAddEvent -> event.retrieveMessage().await()
            is PrivateMessageReactionAddEvent -> event.channel.retrieveMessageById(event.messageIdLong).await()
            else -> return Pair(first = false, second = false)
        }
        if (debug) {
            println("passed message test")
        }
        if (requiredGuild != guild?.idLong && (requiredGuild != 0L && guild != null)) {
            if (debug) {
                println("stopped - requiredGuild: $requiredGuild, guild: ${guild.idLong}")
            }
            return Pair(first = false, second = false)
        }
        if (debug) {
            println("passed requiredGuild test")
        }
        if (requiredMessage != messageId && requiredMessage != 0L) {
            if (debug) {
                println("stopped 2 - requiredMessage: $requiredMessage, messageId: $messageId")
            }
            return Pair(first = false, second = false)
        }
        if (debug) {
            println("passed requiredMessage test")
        }
        val user = jda.retrieveUserById(userId).await()
        val member = guild?.retrieveMemberById(userId)?.await()
        val runAction = { success: Boolean ->
            member?.let {
                if (!success) {
                    return@let
                }
                if (debug) {
                    println("called action - guild")
                }
            } ?: let {
                if (!success) {
                    return@let
                }
                if (debug) {
                    println("called action - null guild")
                }
            }
        }
        if (whitelist.hasAccess(user, member, channel as? GuildChannel)) {
            runAction(true)
            return Pair(
                first = true,
                second = !action(event, guild, emoji, channel, user, msg, member, uuid)
            )
        }
        if (debug) {
            println("no permissions")
        }
        if (removeIfNoPerms && !user.isBot && user.idLong != jda.selfUser.idLong) {
            runAction(false)
            return Pair(first = false, second = false)
        }
        if (user.isBot && removeBotIfNoPerms) {
            runAction(false)
            return Pair(first = false, second = false)
        }
        if (user.idLong == jda.selfUser.idLong && removeSelfIfNoPerms) {
            runAction(false)
            return Pair(first = false, second = false)
        }
        when {
            emoji.isEmoji -> msg.removeReaction(emoji.emoji, user).await()
            emoji.isEmote -> msg.removeReaction(emoji.emote, user).await()
        }
        runAction(false)
        return Pair(first = false, second = false)
    }

    /**
     * Type
     *
     * @property classes
     * @constructor Create empty Type
     */
    enum class Type(@API val classes: Set<KClass<out Event>>) {
        /**
         * R e a c t i o n_a d d
         *
         * @constructor Create empty R e a c t i o n_a d d
         */
        REACTION_ADD(
            setOf(
                MessageReactionAddEvent::class,
                GuildMessageReactionAddEvent::class,
                PrivateMessageReactionAddEvent::class
            )
        )
    }
}