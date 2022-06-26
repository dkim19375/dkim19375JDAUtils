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

@file:Suppress("unused", "DuplicatedCode")

package me.dkim19375.dkim19375jdautils.util

import dev.minn.jda.ktx.await
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.sharding.ShardManager
import java.util.concurrent.CompletableFuture
import java.util.regex.Pattern

val DISCORD_ID: Pattern = Pattern.compile("\\d{17,20}")
val FULL_USER_REF: Pattern = Pattern.compile("(\\S.{0,30}\\S)\\s*#(\\d{4})")
val USER_MENTION: Pattern = Pattern.compile("<@!?(\\d{17,20})>")
val CHANNEL_MENTION: Pattern = Pattern.compile("<#(\\d{17,20})>")
val ROLE_MENTION: Pattern = Pattern.compile("<@&(\\d{17,20})>")
val EMOTE_MENTION: Pattern = Pattern.compile("<:(.{2,32}):(\\d{17,20})>")

private suspend fun ShardManager.retrieveUserById(id: String, cache: Boolean): User? {
    return retrieveUserById(id.toLongOrNull() ?: return null, cache)
}

private suspend fun ShardManager.retrieveUserById(id: Long, cache: Boolean): User? =
    if (cache) getUserById(id) else retrieveUserById(id).await()

fun JDA.findUsersBlocking(query: String, useShardManager: Boolean = false, useCache: Boolean = true): List<User> =
    runBlocking {
        findUsers(query, useShardManager, useCache)
    }

suspend fun JDA.findUsers(query: String, useShardManager: Boolean = false, useCache: Boolean = true): List<User> {
    val userMention = USER_MENTION.matcher(query)
    val fullRefMatch = FULL_USER_REF.matcher(query)
    val manager = if (useShardManager) shardManager else null
    val cache = manager?.userCache ?: userCache
    val users = if (useCache) {
        cache.asList()
    } else {
        (manager?.guilds ?: guilds)
            .map(Guild::loadMembers)
            .map { it.await() }
            .flatten()
            .map(Member::getUser)
    }
    when {
        userMention.matches() -> {
            val user =
                if (manager != null) manager.retrieveUserById(userMention.group(1), useCache)
                else retrieveUserById(userMention.group(1), !useCache).await()
            if (user != null) return listOf(user)
        }
        fullRefMatch.matches() -> {
            val lowerName = fullRefMatch.group(1).lowercase()
            val discriminator = fullRefMatch.group(2)
            users.filter { user: User ->
                user.name.lowercase() == lowerName && user.discriminator == discriminator
            }
            if (users.isNotEmpty()) return users.toList()
        }
        DISCORD_ID.matcher(query).matches() -> {
            val user = if (manager != null) manager.retrieveUserById(query, useCache) else retrieveUserById(
                query,
                !useCache
            ).await()
            if (user != null) return listOf(user)
        }
    }
    val exact = mutableListOf<User>()
    val wrongCase = mutableListOf<User>()
    val startsWith = mutableListOf<User>()
    val contains = mutableListOf<User>()
    val lowerQuery = query.lowercase()
    users.forEach { user: User ->
        val name = user.name
        when {
            name == query -> exact.add(user)
            name.equals(query, ignoreCase = true) && exact.isEmpty() -> wrongCase.add(user)
            name.lowercase().startsWith(lowerQuery) && wrongCase.isEmpty() -> startsWith.add(user)
            name.lowercase().contains(lowerQuery) && startsWith.isEmpty() -> contains.add(user)
        }
    }
    return when {
        exact.isNotEmpty() -> exact
        wrongCase.isNotEmpty() -> wrongCase
        else -> startsWith.ifEmpty { contains }
    }
}

fun Guild.findBannedUsersBlocking(query: String, useCache: Boolean = true): List<User>? {
    val action = CompletableFuture.completedFuture(run {
        runBlocking {
            findBannedUsers(query, useCache)
        }
    })
    return action.get()
}

suspend fun Guild.findBannedUsers(query: String, useCache: Boolean = true): List<User>? {
    var mQuery = query
    val bans: List<User>
    try {
        bans = retrieveBanList().await().map { it.user }.toList()
    } catch (e: InsufficientPermissionException) {
        return null
    }
    var discriminator: String? = null
    val userMention = USER_MENTION.matcher(mQuery)
    when {
        userMention.matches() -> {
            val id = userMention.group(1)
            val user = jda.retrieveUserById(id, !useCache).await()
            if (user != null && bans.contains(user)) return listOf(user)
            for (u in bans) if (u.id == id) return listOf(u)
        }
        FULL_USER_REF.matcher(mQuery).matches() -> {
            discriminator = mQuery.substring(mQuery.length - 4)
            mQuery = mQuery.substring(0, mQuery.length - 5).trim()
        }
        DISCORD_ID.matcher(mQuery).matches() -> {
            val user = jda.retrieveUserById(mQuery, !useCache).await()
            if (user != null && bans.contains(user)) return listOf(user)
            for (u in bans) if (u.id == mQuery) return listOf(u)
        }
    }
    val exact = mutableListOf<User>()
    val wrongCase = mutableListOf<User>()
    val startsWith = mutableListOf<User>()
    val contains = mutableListOf<User>()
    val lowerQuery = mQuery.lowercase()
    for (u in bans) {
        if (discriminator != null && u.discriminator != discriminator) {
            continue
        }
        when {
            u.name == mQuery -> exact.add(u)
            exact.isEmpty() && u.name.equals(mQuery, ignoreCase = true) -> wrongCase.add(u)
            wrongCase.isEmpty() && u.name.lowercase().startsWith(lowerQuery) -> startsWith.add(u)
            startsWith.isEmpty() && u.name.lowercase().contains(lowerQuery) -> contains.add(u)
        }
    }
    return when {
        exact.isNotEmpty() -> exact
        wrongCase.isNotEmpty() -> wrongCase
        else -> startsWith.ifEmpty { contains }
    }
}

fun Guild.findMembersBlocking(query: String, useCache: Boolean = true): List<Member> {
    val action = CompletableFuture.completedFuture(run {
        runBlocking {
            findMembers(query, useCache)
        }
    })
    return action.get()
}

suspend fun Guild.findMembers(query: String, useCache: Boolean = true): List<Member> {
    val userMention = USER_MENTION.matcher(query)
    val fullRefMatch = FULL_USER_REF.matcher(query)
    when {
        userMention.matches() -> {
            val member = retrieveMemberById(userMention.group(1), !useCache).await()
            if (member != null) return listOf(member)
        }
        fullRefMatch.matches() -> {
            val lowerName = fullRefMatch.group(1).lowercase()
            val discriminator = fullRefMatch.group(2)
            val members = (if (useCache) memberCache.toList() else loadMembers().await())
                .filter { member: Member ->
                    member.user.name.lowercase() == lowerName && member.user
                        .discriminator == discriminator
                }
            if (members.isNotEmpty()) return members.toList()
        }
        DISCORD_ID.matcher(query).matches() -> {
            val member = retrieveMemberById(query, !useCache).await()
            if (member != null) {
                return listOf(member)
            }
        }
    }
    val exact = mutableListOf<Member>()
    val wrongCase = mutableListOf<Member>()
    val startsWith = mutableListOf<Member>()
    val contains = mutableListOf<Member>()
    val lowerQuery = query.lowercase()
    (if (useCache) memberCache.toList() else loadMembers().await()).forEach { member: Member ->
        val name = member.user.name
        val effName = member.effectiveName
        when {
            name == query || effName == query -> exact.add(member)
            (name.equals(query, ignoreCase = true) || effName.equals(
                query,
                ignoreCase = true
            )) && exact.isEmpty() -> wrongCase.add(member)
            (name.lowercase().startsWith(lowerQuery) || effName.lowercase()
                .startsWith(lowerQuery)) && wrongCase.isEmpty() -> startsWith.add(member)
            (name.lowercase().contains(lowerQuery) || effName.lowercase()
                .contains(lowerQuery)) && startsWith.isEmpty() -> contains.add(member)
        }
    }
    return when {
        exact.isNotEmpty() -> exact
        wrongCase.isNotEmpty() -> wrongCase
        else -> startsWith.ifEmpty { contains }
    }
}

fun Guild.findTextChannelsBlocking(query: String): List<TextChannel> {
    val action = CompletableFuture.completedFuture(run {
        runBlocking {
            findTextChannels(query)
        }
    })
    return action.get()
}

fun Guild.findTextChannels(query: String): List<TextChannel> {
    val channelMention = CHANNEL_MENTION.matcher(query)
    when {
        channelMention.matches() -> {
            val tc = getTextChannelById(channelMention.group(1))
            if (tc != null) return listOf(tc)
        }
        DISCORD_ID.matcher(query).matches() -> {
            val tc = getTextChannelById(query)
            if (tc != null) return listOf(tc)
        }
    }
    return textChannelCache.asList().findTextChannels(query)
}

fun JDA.findTextChannelsBlocking(query: String, useShardManager: Boolean = false): List<TextChannel> {
    val action = CompletableFuture.completedFuture(run {
        runBlocking {
            findTextChannels(query, useShardManager)
        }
    })
    return action.get()
}

fun JDA.findTextChannels(query: String, useShardManager: Boolean = false): List<TextChannel> {
    val channelMention = CHANNEL_MENTION.matcher(query)
    val manager = if (useShardManager) shardManager else null
    when {
        channelMention.matches() -> {
            val tc =
                if (manager != null) manager.getTextChannelById(channelMention.group(1)) else getTextChannelById(
                    channelMention.group(1)
                )
            if (tc != null) return listOf(tc)
        }
        DISCORD_ID.matcher(query).matches() -> {
            val tc = if (manager != null) manager.getTextChannelById(query) else getTextChannelById(query)
            if (tc != null) return listOf(tc)
        }
    }
    return (manager?.textChannelCache ?: textChannelCache).asList().findTextChannels(query)
}

fun List<TextChannel>.findTextChannelsBlocking(query: String): List<TextChannel> {
    val action = CompletableFuture.completedFuture(run {
        runBlocking {
            findTextChannels(query)
        }
    })
    return action.get()
}

fun List<TextChannel>.findTextChannels(query: String): List<TextChannel> {
    val exact = mutableListOf<TextChannel>()
    val wrongCase = mutableListOf<TextChannel>()
    val startsWith = mutableListOf<TextChannel>()
    val contains = mutableListOf<TextChannel>()
    val lowerQuery = query.lowercase()
    forEach { tc: TextChannel ->
        val name = tc.name
        when {
            name == query -> exact.add(tc)
            name.equals(query, ignoreCase = true) && exact.isEmpty() -> wrongCase.add(tc)
            name.lowercase().startsWith(lowerQuery) && wrongCase.isEmpty() -> startsWith.add(tc)
            name.lowercase().contains(lowerQuery) && startsWith.isEmpty() -> contains.add(tc)
        }
    }
    return when {
        exact.isNotEmpty() -> exact
        wrongCase.isNotEmpty() -> wrongCase
        else -> startsWith.ifEmpty { contains }
    }
}

fun Guild.findVoiceChannelsBlocking(query: String): List<VoiceChannel> {
    val action = CompletableFuture.completedFuture(run {
        runBlocking {
            findVoiceChannels(query)
        }
    })
    return action.get()
}

fun Guild.findVoiceChannels(query: String): List<VoiceChannel> {
    if (DISCORD_ID.matcher(query).matches()) {
        val vc = getVoiceChannelById(query)
        if (vc != null) return listOf(vc)
    }
    return voiceChannelCache.asList().findVoiceChannels(query)
}

fun JDA.findVoiceChannelsBlocking(query: String, useShardManager: Boolean = false): List<VoiceChannel> {
    val action = CompletableFuture.completedFuture(run {
        runBlocking {
            findVoiceChannels(query, useShardManager)
        }
    })
    return action.get()
}

fun JDA.findVoiceChannels(query: String, useShardManager: Boolean = false): List<VoiceChannel> {
    val manager = if (useShardManager) shardManager else null
    if (DISCORD_ID.matcher(query).matches()) {
        val vc = if (manager != null) manager.getVoiceChannelById(query) else getVoiceChannelById(query)
        if (vc != null) return listOf(vc)
    }
    return (manager?.voiceChannelCache ?: voiceChannelCache).asList().findVoiceChannels(query)
}

fun List<VoiceChannel>.findVoiceChannelsBlocking(query: String): List<VoiceChannel> {
    val action = CompletableFuture.completedFuture(run {
        runBlocking {
            findVoiceChannels(query)
        }
    })
    return action.get()
}

fun List<VoiceChannel>.findVoiceChannels(query: String): List<VoiceChannel> {
    val exact = mutableListOf<VoiceChannel>()
    val wrongCase = mutableListOf<VoiceChannel>()
    val startsWith = mutableListOf<VoiceChannel>()
    val contains = mutableListOf<VoiceChannel>()
    val lowerQuery = query.lowercase()
    forEach { vc: VoiceChannel ->
        val name = vc.name
        when {
            name == query -> exact.add(vc)
            name.equals(query, ignoreCase = true) && exact.isEmpty() -> wrongCase.add(vc)
            name.lowercase().startsWith(lowerQuery) && wrongCase.isEmpty() -> startsWith.add(vc)
            name.lowercase().contains(lowerQuery) && startsWith.isEmpty() -> contains.add(vc)
        }
    }
    return when {
        exact.isNotEmpty() -> exact
        wrongCase.isNotEmpty() -> wrongCase
        else -> startsWith.ifEmpty { contains }
    }
}

fun Guild.findCategoriesBlocking(query: String): List<Category> {
    val action = CompletableFuture.completedFuture(run {
        runBlocking {
            findCategories(query)
        }
    })
    return action.get()
}

fun Guild.findCategories(query: String): List<Category> {
    if (DISCORD_ID.matcher(query).matches()) {
        val cat = getCategoryById(query)
        if (cat != null) return listOf(cat)
    }
    return categoryCache.asList().findCategories(query)
}

fun JDA.findCategoriesBlocking(query: String, useShardManager: Boolean = false): List<Category> {
    val action = CompletableFuture.completedFuture(run {
        runBlocking {
            findCategories(query, useShardManager)
        }
    })
    return action.get()
}

fun JDA.findCategories(query: String, useShardManager: Boolean = false): List<Category> {
    val manager = if (useShardManager) shardManager else null
    if (DISCORD_ID.matcher(query).matches()) {
        val cat = if (manager != null) manager.getCategoryById(query) else getCategoryById(query)
        if (cat != null) return listOf(cat)
    }
    return categoryCache.asList().findCategories(query)
}

fun List<Category>.findCategoriesBlocking(query: String): List<Category> {
    val action = CompletableFuture.completedFuture(run {
        runBlocking {
            findCategories(query)
        }
    })
    return action.get()
}

fun List<Category>.findCategories(
    query: String,
): List<Category> {
    val exact = mutableListOf<Category>()
    val wrongCase = mutableListOf<Category>()
    val startsWith = mutableListOf<Category>()
    val contains = mutableListOf<Category>()
    val lowerQuery = query.lowercase()
    forEach { cat: Category ->
        val name = cat.name
        when {
            name == query -> exact.add(cat)
            name.equals(query, ignoreCase = true) && exact.isEmpty() -> wrongCase.add(cat)
            name.lowercase().startsWith(lowerQuery) && wrongCase.isEmpty() -> startsWith.add(cat)
            name.lowercase().contains(lowerQuery) && startsWith.isEmpty() -> contains.add(cat)
        }
    }
    return when {
        exact.isNotEmpty() -> exact
        wrongCase.isNotEmpty() -> wrongCase
        else -> startsWith.ifEmpty { contains }
    }
}

fun Guild.findRolesBlocking(query: String): List<Role> {
    val action = CompletableFuture.completedFuture(run {
        runBlocking {
            findRoles(query)
        }
    })
    return action.get()
}

fun Guild.findRoles(query: String): List<Role> {
    val roleMention = ROLE_MENTION.matcher(query)
    if (roleMention.matches()) {
        val role = getRoleById(roleMention.group(1))
        if (role != null) return listOf(role)
    } else if (DISCORD_ID.matcher(query).matches()) {
        val role = getRoleById(query)
        if (role != null) return listOf(role)
    }
    val exact = mutableListOf<Role>()
    val wrongCase = mutableListOf<Role>()
    val startsWith = mutableListOf<Role>()
    val contains = mutableListOf<Role>()
    val lowerQuery = query.lowercase()
    roleCache.forEach { role: Role ->
        val name = role.name
        when {
            name == query -> exact.add(role)
            name.equals(query, ignoreCase = true) && exact.isEmpty() -> wrongCase.add(role)
            name.lowercase().startsWith(lowerQuery) && wrongCase.isEmpty() -> startsWith.add(role)
            name.lowercase().contains(lowerQuery) && startsWith.isEmpty() -> contains.add(role)
        }
    }
    return when {
        exact.isNotEmpty() -> exact
        wrongCase.isNotEmpty() -> wrongCase
        else -> startsWith.ifEmpty { contains }
    }
}

fun Guild.findEmojisBlocking(query: String): List<Emoji> = runBlocking {
    findEmojis(query)
}

suspend fun Guild.findEmojis(query: String, useCache: Boolean = true): List<Emoji> {
    val mentionMatcher = EMOTE_MENTION.matcher(query)
    when {
        DISCORD_ID.matcher(query).matches() -> {
            val emoji = getEmojiById(query)
            if (emoji != null) return listOf(emoji)
        }
        mentionMatcher.matches() -> {
            val emoteName = mentionMatcher.group(1)
            val emoteId = mentionMatcher.group(2)
            val emoji = getEmojiById(emoteId)
            if (emoji != null && emoji.name == emoteName) return listOf(emoji)
        }
    }
    return (if (useCache) emojiCache.asList() else retrieveEmojis().await()).findEmojis(query)
}

fun JDA.findEmojisBlocking(query: String, useShardManager: Boolean = false, useCache: Boolean = true): List<Emoji> {
    val action = CompletableFuture.completedFuture(run {
        runBlocking {
            findEmojis(query, useShardManager, useCache)
        }
    })
    return action.get()
}

suspend fun JDA.findEmojis(query: String, useShardManager: Boolean = false, useCache: Boolean = true): List<Emoji> {
    val mentionMatcher = EMOTE_MENTION.matcher(query)
    val manager = if (useShardManager) shardManager else null
    when {
        DISCORD_ID.matcher(query).matches() -> {
            val emoji = if (manager != null) manager.getEmojiById(query) else getEmojiById(query)
            if (emoji != null) return listOf(emoji)
        }
        mentionMatcher.matches() -> {
            val emoteName = mentionMatcher.group(1)
            val emoteId = mentionMatcher.group(2)
            val emoji = if (manager != null) manager.getEmojiById(emoteId) else getEmojiById(emoteId)
            if (emoji != null && emoji.name == emoteName) return listOf(emoji)
        }
    }
    return (if (useCache) {
        emojiCache.asList()
    } else {
        guilds
            .mapNotNull(Guild::retrieveEmojis)
            .mapNotNull {
                try {
                    it.await()
                } catch (t: Throwable) {
                    null
                }
            }
            .flatten()
            .map {
                it as Emoji
            }
    }).findEmojis(query)
}

fun List<Emoji>.findEmojisBlocking(query: String): List<Emoji> {
    val action = CompletableFuture.completedFuture(run {
        runBlocking {
            findEmojis(query)
        }
    })
    return action.get()
}

fun List<Emoji>.findEmojis(
    query: String,
): List<Emoji> {
    val exact = mutableListOf<Emoji>()
    val wrongCase = mutableListOf<Emoji>()
    val startsWith = mutableListOf<Emoji>()
    val contains = mutableListOf<Emoji>()
    val lowerQuery = query.lowercase()
    forEach { Emoji: Emoji ->
        val name = Emoji.name
        when {
            name == query -> exact.add(Emoji)
            name.equals(query, ignoreCase = true) && exact.isEmpty() -> wrongCase.add(Emoji)
            name.lowercase().startsWith(lowerQuery) && wrongCase.isEmpty() -> startsWith.add(Emoji)
            name.lowercase().contains(lowerQuery) && startsWith.isEmpty() -> contains.add(Emoji)
        }
    }
    return when {
        exact.isNotEmpty() -> exact
        wrongCase.isNotEmpty() -> wrongCase
        else -> startsWith.ifEmpty { contains }
    }
}