package me.dkim19375.dkim19375jdautils.util

import me.dkim19375.dkim19375jdautils.BotBase
import me.dkim19375.dkim19375jdautils.command.Command
import me.dkim19375.dkim19375jdautils.command.CommandType
import java.util.*

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
fun <K, V> MutableMap<K, V>.removeIf(filter: (K, V) -> Boolean) {
    for ((k, v) in toMap()) {
        if (filter(k, v)) {
            remove(k)
        }
    }
}

fun Collection<UUID>.getRandomUUID(): UUID {
    while (true) {
        val uuid = UUID.randomUUID()
        if (contains(uuid)) {
            continue
        }
        return uuid
    }
}

fun <T> Collection<Collection<T>>.combine(): List<T> {
    val new = mutableListOf<T>()
    for (item in this) {
        new.addAll(item)
    }
    return new
}