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