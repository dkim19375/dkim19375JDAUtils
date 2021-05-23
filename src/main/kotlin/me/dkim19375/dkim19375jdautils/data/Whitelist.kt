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
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.GuildChannel
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User

data class Whitelist(
    val jda: JDA,
    val permissions: Set<Permission> = emptySet(),
    val whitelist: Set<Long>? = null,
    val blacklist: Set<Long> = emptySet(),
    val ignoreWhitelist: Set<Long> = emptySet(),
    val ignoreWhitelistBots: Boolean = true,
    val ignoreWhitelistSelf: Boolean = true
) {
    fun hasAccess(
        user: User,
        member: Member? = null,
        channel: GuildChannel? = null
    ): Boolean {
        if (member != null) {
            if (!member.hasPermission(permissions, channel)) {
                return false
            }
        }
        if (
            whitelist != null
            && (!ignoreWhitelist.contains(user.idLong))
            && (!(user.isBot && ignoreWhitelistBots))
            && (user.idLong == jda.selfUser.idLong && !ignoreWhitelistSelf)
            && (!whitelist.contains(user.idLong))
        ) {
            return false
        }
        if (blacklist.contains(user.idLong)) {
            return false
        }
        return true
    }
}