package me.dkim19375.dkim19375jdautils.embeds;

import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EmbedUtils {
    public static Set<MessageEmbed.Field> getEmbedGroups(Map<String, Set<String>> set) {
        Set<MessageEmbed.Field> fields = new HashSet<>();
        for (Map.Entry<String, Set<String>> group : set.entrySet()) {
            final String name = group.getKey();
            String value = "```\n- ";
            for (String string : group.getValue()) {
                value = combineStrings(value, string + "\n- ");
            }
            value = combineStrings(value, "```");
            fields.add(new MessageEmbed.Field(name, value, true));
        }
        return fields;
    }

    private static String combineStrings(String first, String second) {
        return first + second;
    }

    public static MessageEmbed.Field getEmbedGroup(Map.Entry<String, Set<String>> group) {
        final String name = group.getKey();
        String value = "```\n- ";
        for (String string : group.getValue()) {
            value = combineStrings(value, string + "\n- ");
        }
        value = combineStrings(value, "```");
        return new MessageEmbed.Field(name, value, true);
    }
}