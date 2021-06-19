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

package me.dkim19375.dkim19375jdautils.data

import me.dkim19375.dkim19375jdautils.util.hasPermission
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.GuildChannel
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User

/**
 * Whitelist
 *
 * By default it applies to all users
 *
 * @property permissions Permissions required
 * @property whitelist If it is not null, then the user's id must be in the [Set<Long>][Set]
 * @property blacklist The user's id must not be in the [Set<Long>][Set]
 * @property ignore Ignores whitelist rules
 * @property ignoreWhitelistBots Makes bots ignore whitelist rules
 * @property ignoreWhitelistSelf Makes self user ignore whitelist rules
 * @constructor Creates an empty whitelist instance
 */
data class Whitelist(
    val permissions: Set<Permission> = emptySet(),
    val whitelist: Set<Long>? = null,
    val blacklist: Set<Long> = emptySet(),
    val ignore: Pair<IgnoreType, Set<Long>> = Pair(IgnoreType.IGNORE_WHITELIST, emptySet()),
    val ignoreWhitelistBots: IgnoreType? = null,
    val ignoreWhitelistSelf: IgnoreType? = null
) {
    fun hasAccess(
        user: User,
        member: Member? = null,
        channel: GuildChannel? = null
    ): Boolean {
        val isSelf = user.idLong == user.jda.selfUser.idLong
        val ignoreType = ignore.first
        val ignore = ignore.second
        if (ignore.contains(user.idLong) && ignoreType == IgnoreType.IGNORE_ALL) {
            return true
        }
        if (ignoreWhitelistBots == IgnoreType.IGNORE_ALL && user.isBot) {
            return true
        }
        if (ignoreWhitelistSelf == IgnoreType.IGNORE_ALL && isSelf) {
            return true
        }
        val ignoreWhitelistBots = ignoreWhitelistBots != null
        val ignoreWhitelistSelf = ignoreWhitelistSelf != null
        if (member != null) {
            if (!member.hasPermission(permissions, channel)) {
                return false
            }
        }
        if (
            (whitelist != null) // checks if whitelist is not null
            && (!ignore.contains(user.idLong)) // checks if the user is in the bypass whitelist
            && (!(user.isBot && ignoreWhitelistBots)) // checks if the user is a bot and is in the bypass whitelist
            && (!(isSelf && ignoreWhitelistSelf)) // checks if the user is self and is in the bypass whitelist
            && (!whitelist.contains(user.idLong)) // checks if the user is not in the whitelist
        ) {
            return false
        }
        return !blacklist.contains(user.idLong)
    }

    enum class IgnoreType {
        /**
         * Always returns true, ignores the entire whitelist, even if they don't have permissions
         */
        IGNORE_ALL,

        /**
         * Ignores only the whitelist, will return false if users do not have permission
         */
        IGNORE_WHITELIST
    }
}