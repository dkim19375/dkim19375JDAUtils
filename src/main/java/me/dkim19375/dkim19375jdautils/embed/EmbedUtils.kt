package me.dkim19375.dkim19375jdautils.embed

import me.dkim19375.dkim19375jdautils.annotation.API
import net.dv8tion.jda.api.entities.MessageEmbed

@API
object EmbedUtils {
    @API
    fun getEmbedGroups(groups: Map<String, Collection<String>>): Set<MessageEmbed.Field> {
        val fields: MutableSet<MessageEmbed.Field> = mutableSetOf()
        for (group in groups.entries) {
            fields.add(getEmbedGroup(group.key, group.value))
        }
        return fields
    }

    private fun combineStrings(first: String, second: String): String {
        return "$first$second"
    }

    @API
    fun getEmbedGroup(name: String, values: Collection<String>): MessageEmbed.Field {
        if (values.isEmpty()) {
            return MessageEmbed.Field(name, "```\nNone ```", false)
        }
        var value = "```\n- "
        var i = 1
        for (string in values) {
            value = if (i == values.size) {
                combineStrings(value, string)
            } else {
                combineStrings(value, "$string\n- ")
            }
            i++
        }
        value = combineStrings(value, "```")
        return MessageEmbed.Field(name, value, false)
    }
}