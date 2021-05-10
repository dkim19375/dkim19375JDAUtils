package me.dkim19375.dkim19375jdautils.embeds;

import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class EmbedUtils {
    @NotNull
    public static Set<MessageEmbed.@NotNull Field> getEmbedGroups(final @NotNull Map<@NotNull String, ? extends Collection<@NotNull String>> groups) {
        final Set<MessageEmbed.Field> fields = new HashSet<>();
        for (Map.Entry<String, ? extends Collection<String>> group : groups.entrySet()) {
            fields.add(getEmbedGroup(group));
        }
        return fields;
    }

    private static String combineStrings(String first, String second) {
        return first + second;
    }

    @NotNull
    public static MessageEmbed.Field getEmbedGroup(@NotNull final Map.Entry<@NotNull String, ? extends Collection<@NotNull String>> group) {
        final String name = group.getKey();
        String value = "```\n- ";
        int i = 1;
        for (String string : group.getValue()) {
            if (i == group.getValue().size()) {
                value = combineStrings(value, string);
            } else {
                value = combineStrings(value, string + "\n- ");
            }
            i++;
        }
        value = combineStrings(value, "```");
        return new MessageEmbed.Field(name, value, true);
    }
}