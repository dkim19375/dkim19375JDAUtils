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

fun JDA.findUsersBlocking(query: String, useShardManager: Boolean = false, useCache: Boolean = true): List<User> {
    val action = CompletableFuture.completedFuture(run {
        runBlocking {
            findUsers(query, useShardManager, useCache)
        }
    })
    return action.get()
}

suspend fun JDA.findUsers(query: String, useShardManager: Boolean = false, useCache: Boolean = true): List<User> {
    val userMention = USER_MENTION.matcher(query)
    val fullRefMatch = FULL_USER_REF.matcher(query)
    val manager = if (useShardManager) shardManager else null
    val cache = manager?.userCache ?: userCache
    val users = if (useCache) {
        cache.asList()
    } else {
        manager?.guilds
            ?.map(Guild::loadMembers)
            ?.map { it.await() }
            ?.combine()
            ?.map(Member::getUser)
            ?: guilds.map(Guild::loadMembers)
                .map { it.await() }
                .combine()
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
            val discrim = fullRefMatch.group(2)
            users.filter { user: User ->
                user.name.lowercase() == lowerName && user.discriminator == discrim
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
    val wrongcase = mutableListOf<User>()
    val startswith = mutableListOf<User>()
    val contains = mutableListOf<User>()
    val lowerquery = query.lowercase()
    users.forEach { user: User ->
        val name = user.name
        when {
            name == query -> exact.add(user)
            name.equals(query, ignoreCase = true) && exact.isEmpty() -> wrongcase.add(user)
            name.lowercase().startsWith(lowerquery) && wrongcase.isEmpty() -> startswith.add(user)
            name.lowercase().contains(lowerquery) && startswith.isEmpty() -> contains.add(user)
        }
    }
    return when {
        exact.isNotEmpty() -> exact
        wrongcase.isNotEmpty() -> wrongcase
        else -> startswith.ifEmpty { contains }
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
    var discrim: String? = null
    val userMention = USER_MENTION.matcher(mQuery)
    when {
        userMention.matches() -> {
            val id = userMention.group(1)
            val user = jda.retrieveUserById(id, !useCache).await()
            if (user != null && bans.contains(user)) return listOf(user)
            for (u in bans) if (u.id == id) return listOf(u)
        }
        FULL_USER_REF.matcher(mQuery).matches() -> {
            discrim = mQuery.substring(mQuery.length - 4)
            mQuery = mQuery.substring(0, mQuery.length - 5).trim()
        }
        DISCORD_ID.matcher(mQuery).matches() -> {
            val user = jda.retrieveUserById(mQuery, !useCache).await()
            if (user != null && bans.contains(user)) return listOf(user)
            for (u in bans) if (u.id == mQuery) return listOf(u)
        }
    }
    val exact = mutableListOf<User>()
    val wrongcase = mutableListOf<User>()
    val startswith = mutableListOf<User>()
    val contains = mutableListOf<User>()
    val lowerQuery = mQuery.lowercase()
    for (u in bans) {
        if (discrim != null && u.discriminator != discrim) {
            continue
        }
        when {
            u.name == mQuery -> exact.add(u)
            exact.isEmpty() && u.name.equals(mQuery, ignoreCase = true) -> wrongcase.add(u)
            wrongcase.isEmpty() && u.name.lowercase().startsWith(lowerQuery) -> startswith.add(u)
            startswith.isEmpty() && u.name.lowercase().contains(lowerQuery) -> contains.add(u)
        }
    }
    return when {
        exact.isNotEmpty() -> exact
        wrongcase.isNotEmpty() -> wrongcase
        else -> startswith.ifEmpty { contains }
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
            val discrim = fullRefMatch.group(2)
            val members = (if (useCache) memberCache.toList() else loadMembers().await())
                .filter { member: Member ->
                    member.user.name.lowercase() == lowerName && member.user
                        .discriminator == discrim
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
    val wrongcase = mutableListOf<Member>()
    val startswith = mutableListOf<Member>()
    val contains = mutableListOf<Member>()
    val lowerquery = query.lowercase()
    (if (useCache) memberCache.toList() else loadMembers().await()).forEach { member: Member ->
        val name = member.user.name
        val effName = member.effectiveName
        when {
            name == query || effName == query -> exact.add(member)
            (name.equals(query, ignoreCase = true) || effName.equals(
                query,
                ignoreCase = true
            )) && exact.isEmpty() -> wrongcase.add(member)
            (name.lowercase().startsWith(lowerquery) || effName.lowercase()
                .startsWith(lowerquery)) && wrongcase.isEmpty() -> startswith.add(member)
            (name.lowercase().contains(lowerquery) || effName.lowercase()
                .contains(lowerquery)) && startswith.isEmpty() -> contains.add(member)
        }
    }
    return when {
        exact.isNotEmpty() -> exact
        wrongcase.isNotEmpty() -> wrongcase
        else -> startswith.ifEmpty { contains }
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
    val wrongcase = mutableListOf<TextChannel>()
    val startswith = mutableListOf<TextChannel>()
    val contains = mutableListOf<TextChannel>()
    val lowerquery = query.lowercase()
    forEach { tc: TextChannel ->
        val name = tc.name
        when {
            name == query -> exact.add(tc)
            name.equals(query, ignoreCase = true) && exact.isEmpty() -> wrongcase.add(tc)
            name.lowercase().startsWith(lowerquery) && wrongcase.isEmpty() -> startswith.add(tc)
            name.lowercase().contains(lowerquery) && startswith.isEmpty() -> contains.add(tc)
        }
    }
    return when {
        exact.isNotEmpty() -> exact
        wrongcase.isNotEmpty() -> wrongcase
        else -> startswith.ifEmpty { contains }
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
    val wrongcase = mutableListOf<VoiceChannel>()
    val startswith = mutableListOf<VoiceChannel>()
    val contains = mutableListOf<VoiceChannel>()
    val lowerquery = query.lowercase()
    forEach { vc: VoiceChannel ->
        val name = vc.name
        when {
            name == query -> exact.add(vc)
            name.equals(query, ignoreCase = true) && exact.isEmpty() -> wrongcase.add(vc)
            name.lowercase().startsWith(lowerquery) && wrongcase.isEmpty() -> startswith.add(vc)
            name.lowercase().contains(lowerquery) && startswith.isEmpty() -> contains.add(vc)
        }
    }
    return when {
        exact.isNotEmpty() -> exact
        wrongcase.isNotEmpty() -> wrongcase
        else -> startswith.ifEmpty { contains }
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
    val wrongcase = mutableListOf<Category>()
    val startswith = mutableListOf<Category>()
    val contains = mutableListOf<Category>()
    val lowerquery = query.lowercase()
    forEach { cat: Category ->
        val name = cat.name
        when {
            name == query -> exact.add(cat)
            name.equals(query, ignoreCase = true) && exact.isEmpty() -> wrongcase.add(cat)
            name.lowercase().startsWith(lowerquery) && wrongcase.isEmpty() -> startswith.add(cat)
            name.lowercase().contains(lowerquery) && startswith.isEmpty() -> contains.add(cat)
        }
    }
    return when {
        exact.isNotEmpty() -> exact
        wrongcase.isNotEmpty() -> wrongcase
        else -> startswith.ifEmpty { contains }
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
    val wrongcase = mutableListOf<Role>()
    val startswith = mutableListOf<Role>()
    val contains = mutableListOf<Role>()
    val lowerquery = query.lowercase()
    roleCache.forEach { role: Role ->
        val name = role.name
        when {
            name == query -> exact.add(role)
            name.equals(query, ignoreCase = true) && exact.isEmpty() -> wrongcase.add(role)
            name.lowercase().startsWith(lowerquery) && wrongcase.isEmpty() -> startswith.add(role)
            name.lowercase().contains(lowerquery) && startswith.isEmpty() -> contains.add(role)
        }
    }
    return when {
        exact.isNotEmpty() -> exact
        wrongcase.isNotEmpty() -> wrongcase
        else -> startswith.ifEmpty { contains }
    }
}

fun Guild.findEmotesBlocking(query: String): List<Emote> {
    val action = CompletableFuture.completedFuture(run {
        runBlocking {
            findEmotes(query)
        }
    })
    return action.get()
}

suspend fun Guild.findEmotes(query: String, useCache: Boolean = true): List<Emote> {
    val mentionMatcher = EMOTE_MENTION.matcher(query)
    when {
        DISCORD_ID.matcher(query).matches() -> {
            val emote = getEmoteById(query)
            if (emote != null) return listOf(emote)
        }
        mentionMatcher.matches() -> {
            val emoteName = mentionMatcher.group(1)
            val emoteId = mentionMatcher.group(2)
            val emote = getEmoteById(emoteId)
            if (emote != null && emote.name == emoteName) return listOf(emote)
        }
    }
    return (if (useCache) emoteCache.asList() else retrieveEmotes().await()).findEmotes(query)
}

fun JDA.findEmotesBlocking(query: String, useShardManager: Boolean = false, useCache: Boolean = true): List<Emote> {
    val action = CompletableFuture.completedFuture(run {
        runBlocking {
            findEmotes(query, useShardManager, useCache)
        }
    })
    return action.get()
}

suspend fun JDA.findEmotes(query: String, useShardManager: Boolean = false, useCache: Boolean = true): List<Emote> {
    val mentionMatcher = EMOTE_MENTION.matcher(query)
    val manager = if (useShardManager) shardManager else null
    when {
        DISCORD_ID.matcher(query).matches() -> {
            val emote = if (manager != null) manager.getEmoteById(query) else getEmoteById(query)
            if (emote != null) return listOf(emote)
        }
        mentionMatcher.matches() -> {
            val emoteName = mentionMatcher.group(1)
            val emoteId = mentionMatcher.group(2)
            val emote = if (manager != null) manager.getEmoteById(emoteId) else getEmoteById(emoteId)
            if (emote != null && emote.name == emoteName) return listOf(emote)
        }
    }
    return (if (useCache) {
        emoteCache.asList()
    } else {
        guilds
            .map(Guild::retrieveEmotes)
            .map { it.await() }
            .combine()
            .map {
                it as Emote
            }
    }).findEmotes(query)
}

fun List<Emote>.findEmotesBlocking(query: String): List<Emote> {
    val action = CompletableFuture.completedFuture(run {
        runBlocking {
            findEmotes(query)
        }
    })
    return action.get()
}

fun List<Emote>.findEmotes(
    query: String,
): List<Emote> {
    val exact = mutableListOf<Emote>()
    val wrongcase = mutableListOf<Emote>()
    val startswith = mutableListOf<Emote>()
    val contains = mutableListOf<Emote>()
    val lowerquery = query.lowercase()
    forEach { emote: Emote ->
        val name = emote.name
        when {
            name == query -> exact.add(emote)
            name.equals(query, ignoreCase = true) && exact.isEmpty() -> wrongcase.add(emote)
            name.lowercase().startsWith(lowerquery) && wrongcase.isEmpty() -> startswith.add(emote)
            name.lowercase().contains(lowerquery) && startswith.isEmpty() -> contains.add(emote)
        }
    }
    return when {
        exact.isNotEmpty() -> exact
        wrongcase.isNotEmpty() -> wrongcase
        else -> startswith.ifEmpty { contains }
    }
}