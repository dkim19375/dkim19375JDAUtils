package me.dkim19375.dkim19375jdautils.embed

import me.dkim19375.dkim19375jdautils.annotation.API
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.User
import java.awt.Color
import java.time.Instant
import java.time.temporal.TemporalAccessor

@API
class EmbedManager {
    @API
    val embedBuilder = EmbedBuilder()

    @API
    constructor(
        heading: String? = null, headingIcon: String? = null, color: Color? = null,
        cmd: String? = null,
        title: String? = null, titleURL: String? = null,
        description: CharSequence? = null, footerIconURL: String? = null, image: String? = null,
        thumbnail: String? = null
    ) {
        embedBuilder.setAuthor(heading, null, headingIcon)
        embedBuilder.setTimestamp(Instant.now())
        embedBuilder.setColor(color)
        embedBuilder.setFooter(cmd, footerIconURL)
        embedBuilder.setDescription(description)
        embedBuilder.setImage(image)
        embedBuilder.setThumbnail(thumbnail)
        embedBuilder.setTitle(title, titleURL)
    }

    @API
    constructor(
        title: String? = null, color: Color? = null, cmd: String? = null,
        user: User? = null
    ) {
        if (user != null) {
            embedBuilder.setAuthor(user.asTag, null, user.avatarUrl)
        }
        embedBuilder.setTimestamp(Instant.now())
        embedBuilder.setColor(color)
        embedBuilder.setFooter(cmd)
        embedBuilder.setTitle(title)
    }

    @API
    fun setUser(user: User?) {
        if (user == null) {
            embedBuilder.setAuthor(null)
            return
        }
        embedBuilder.setAuthor(user.name + "#" + user.discriminator)
    }

    @API
    fun setTimeStamp(temporalAccessor: TemporalAccessor?) {
        embedBuilder.setTimestamp(temporalAccessor)
    }

    @API
    fun setColor(color: Color?) {
        embedBuilder.setColor(color)
    }

    @API
    fun setFooter(footer: String?) {
        embedBuilder.setFooter(footer)
    }

    @API
    fun setFooter(footer: String?, footerURL: String?) {
        embedBuilder.setFooter(footer, footerURL)
    }

    @API
    fun setDescription(description: CharSequence?) {
        embedBuilder.setDescription(description)
    }

    @API
    fun setImage(imageUrl: String?) {
        embedBuilder.setImage(imageUrl)
    }

    @API
    fun setThumbnail(imageUrl: String?) {
        embedBuilder.setThumbnail(imageUrl)
    }

    @API
    fun setTitle(title: String?) {
        embedBuilder.setTitle(title)
    }

    @API
    fun setTitle(title: String?, titleUrl: String?) {
        embedBuilder.setTitle(title, titleUrl)
    }

    @API
    fun setEmbedBuilder(embedBuilder: EmbedBuilder) {
        this.embedBuilder.clear()
        val messageEmbed = embedBuilder.build()
        this.embedBuilder.setDescription(messageEmbed.description)
        this.embedBuilder.setTitle(messageEmbed.title, messageEmbed.url)
        this.embedBuilder.setTimestamp(messageEmbed.timestamp)
        this.embedBuilder.setColor(messageEmbed.color)
        if (messageEmbed.thumbnail != null) {
            this.embedBuilder.setThumbnail(messageEmbed.thumbnail?.url)
        }
        if (messageEmbed.author != null) {
            this.embedBuilder.setAuthor(
                messageEmbed.author?.name, messageEmbed.author?.url, messageEmbed.author?.iconUrl
            )
        }
        if (messageEmbed.footer != null) {
            this.embedBuilder.setFooter(messageEmbed.footer?.text, messageEmbed.footer?.iconUrl)
        }
        if (messageEmbed.image != null) {
            this.embedBuilder.setImage(messageEmbed.image?.url)
        }
        this.embedBuilder.fields.addAll(messageEmbed.fields)
    }
}