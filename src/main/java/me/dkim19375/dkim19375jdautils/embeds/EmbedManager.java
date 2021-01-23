package me.dkim19375.dkim19375jdautils.embeds;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.time.Instant;
import java.time.temporal.TemporalAccessor;

public class EmbedManager {
    private final EmbedBuilder embedBuilder = new EmbedBuilder();

    public EmbedManager(final @Nullable String heading, final @Nullable String headingIcon, final @Nullable Color color,
                        final @Nullable String cmd,
                        final @Nullable String title, final @Nullable String titleURL,
                        final @Nullable CharSequence description, final @Nullable String footerIconURL, final @Nullable String image,
                        final @Nullable String thumbnail) {
        embedBuilder.setAuthor(heading, null, headingIcon);
        embedBuilder.setTimestamp(Instant.now());
        embedBuilder.setColor(color);
        embedBuilder.setFooter(cmd, footerIconURL);
        embedBuilder.setDescription(description);
        embedBuilder.setImage(image);
        embedBuilder.setThumbnail(thumbnail);
        embedBuilder.setTitle(title, titleURL);
    }

    public EmbedManager(final @Nullable User user, final @Nullable Color color, final @Nullable String cmd,
                        final @Nullable String title) {
        if (user != null) {
            embedBuilder.setAuthor(user.getName(), null, user.getAvatarUrl());
        }
        embedBuilder.setTimestamp(Instant.now());
        embedBuilder.setColor(color);
        embedBuilder.setFooter(cmd);
        embedBuilder.setTitle(title);
    }

    @NotNull
    public EmbedBuilder getEmbedBuilder() {
        return embedBuilder;
    }

    public void setUser(final @NotNull User user) {
        embedBuilder.setAuthor(user.getName() + "#" + user.getDiscriminator());
    }

    public void setTimeStamp(TemporalAccessor temporalAccessor) {
        embedBuilder.setTimestamp(temporalAccessor);
    }

    public void setColor(Color color) {
        embedBuilder.setColor(color);
    }

    public void setFooter(@Nullable String footer) {
        embedBuilder.setFooter(footer);
    }

    public void setFooter(@Nullable String footer, @Nullable String footerURL) {
        embedBuilder.setFooter(footer, footerURL);
    }

    public void setDescription(CharSequence description) {
        embedBuilder.setDescription(description);
    }

    public void setImage(String imageUrl) {
        embedBuilder.setImage(imageUrl);
    }

    public void setThumbnail(String imageUrl) {
        embedBuilder.setThumbnail(imageUrl);
    }

    public void setTitle(String title) {
        embedBuilder.setTitle(title);
    }

    public void setTitle(String title, String titleUrl) {
        embedBuilder.setTitle(title, titleUrl);
    }

    public void setEmbedBuilder(final @NotNull EmbedBuilder embedBuilder) {
        this.embedBuilder.clear();
        final MessageEmbed messageEmbed = embedBuilder.build();
        this.embedBuilder.setDescription(messageEmbed.getDescription());
        this.embedBuilder.setTitle(messageEmbed.getTitle(), messageEmbed.getUrl());
        this.embedBuilder.setTimestamp(messageEmbed.getTimestamp());
        this.embedBuilder.setColor(messageEmbed.getColor());
        if (messageEmbed.getThumbnail() != null) {
            this.embedBuilder.setThumbnail(messageEmbed.getThumbnail().getUrl());
        }
        if (messageEmbed.getAuthor() != null) {
            this.embedBuilder.setAuthor(messageEmbed.getAuthor().getName(), messageEmbed.getAuthor().getUrl(), messageEmbed.getAuthor().getIconUrl());
        }
        if (messageEmbed.getFooter() != null) {
            this.embedBuilder.setFooter(messageEmbed.getFooter().getText(), messageEmbed.getFooter().getIconUrl());
        }
        if (messageEmbed.getImage() != null) {
            this.embedBuilder.setImage(messageEmbed.getImage().getUrl());
        }
        this.embedBuilder.getFields().addAll(messageEmbed.getFields()); }

}