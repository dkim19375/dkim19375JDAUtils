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