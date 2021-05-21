package me.dkim19375.dkim19375jdautils.embed

import me.dkim19375.dkim19375jdautils.annotation.API
import net.dv8tion.jda.api.entities.MessageEmbed

@API
object EmbedUtils {
    @API
    fun getEmbedGroups(groups: Map<String, Collection<String?>?>): Set<MessageEmbed.Field> {
        val fields: MutableSet<MessageEmbed.Field> = mutableSetOf()
        for (group in groups.entries) {
            fields.add(getEmbedGroup(group.key, group.value))
        }
        return fields
    }

    @API
    fun getEmbedGroup(name: String, values: Collection<String?>?): MessageEmbed.Field {
        if (values.isNullOrEmpty()) {
            return MessageEmbed.Field(name, "```\nNone ```", false)
        }
        var value = "```\n- "
        var i = 1
        for (string in values) {
            string ?: continue
            value = if (i == values.size) {
                "$value$string"
            } else {
                "$value$string\n- "
            }
            i++
        }
        value = "$value```"
        return MessageEmbed.Field(name, value, false)
    }
}