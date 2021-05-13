package me.dkim19375.dkim19375jdautils.command

import me.dkim19375.dkim19375jdautils.BotBase
import org.apache.commons.lang3.StringUtils

fun String.getCommandType(bot: BotBase): CommandType? {
    for (type in bot.commandTypes) {
        if (type.name.equals(this, ignoreCase = true)) {
            return type
        }
        if (type.displayname.equals(this, ignoreCase = true)) {
            return type
        }
    }
    return null
}

abstract class CommandType(val name: String, val displayname: String = StringUtils.capitalize(name.lowercase()))