package me.dkim19375.dkim19375jdautils.util

import me.dkim19375.dkim19375jdautils.BotBase
import me.dkim19375.dkim19375jdautils.command.Command
import me.dkim19375.dkim19375jdautils.command.CommandType

fun Iterable<String>.containsIgnoreCase(find: String): Boolean = getIgnoreCase(find) != null
fun Iterable<String>.getIgnoreCase(find: String): String? = firstOrNull { it.equals(find, ignoreCase = true) }
fun String.getCommand(bot: BotBase): Command? = bot.commands.getCommand(this)
fun Set<Command>.getCommand(name: String): Command? {
    return firstOrNull { cmd ->
        cmd.name.equals(name, ignoreCase = true)
                || cmd.command.equals(name, ignoreCase = true)
                || cmd.aliases.containsIgnoreCase(name)
    }
}
fun Set<Command>.getOfType(type: CommandType): Set<Command> {
    val ofType = mutableSetOf<Command>()
    for (cmd in this) {
        if (cmd.type == type) {
            ofType.add(cmd)
        }
    }
    return ofType
}