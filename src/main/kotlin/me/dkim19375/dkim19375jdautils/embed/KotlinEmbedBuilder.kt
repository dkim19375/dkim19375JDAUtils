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

package me.dkim19375.dkim19375jdautils.embed

import me.dkim19375.dkimcore.annotation.API
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.EmbedType
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.MessageEmbed.*
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.internal.entities.EntityBuilder
import net.dv8tion.jda.internal.utils.Checks
import java.awt.Color
import java.time.*
import java.time.temporal.TemporalAccessor
import java.util.*

private val URL_PATTERN = Regex("\\s*(https?|attachment)://\\S+\\s*", RegexOption.IGNORE_CASE)

/**
 * Kotlin embed builder
 *
 * Image: [https://i.imgur.com/9ZAmt5X.png](https://i.imgur.com/9ZAmt5X.png)
 *
 * @param embed A [MessageEmbed] to copy values from
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
class KotlinEmbedBuilder(embed: MessageEmbed? = null) {
    val fields: MutableList<Field> = mutableListOf()
    private val description = StringBuilder()
    var color: Int = Role.DEFAULT_COLOR_RAW
    var url: String? = null
    var title: String? = null
    var timestamp: OffsetDateTime? = null
    var thumbnail: Thumbnail? = null
    var author: AuthorInfo? = null
    var footer: Footer? = null
        private set
    var image: ImageInfo? = null

    init {
        embed?.let { msg ->
            description.append(msg.description)
            url = msg.url
            title = msg.title
            timestamp = msg.timestamp
            color = msg.colorRaw
            thumbnail = msg.thumbnail
            author = msg.author
            footer = msg.footer
            image = msg.image
            fields.addAll(msg.fields)
        }
    }

    @API
    constructor(builder: EmbedBuilder) : this(builder.build())

    companion object {
        /**
         * Get preset #1
         *
         * Image: [https://i.imgur.com/XNrKXVu.png](https://i.imgur.com/XNrKXVu.png)
         */
        fun getFirstPreset(
            title: String? = null,
            color: Color? = null,
            cmd: String? = null,
            user: User? = null,
            fields: List<Field> = emptyList()
        ): KotlinEmbedBuilder {
            val builder = KotlinEmbedBuilder()
            builder.setAuthorSafe(user?.asTag, null, user?.avatarUrl)
            builder.setCurrentTimestamp()
            builder.setColor(color)
            builder.setFooter(cmd)
            builder.title = title
            builder.fields.addAll(fields)
            return builder
        }

        /**
         * Get preset #1
         *
         * Images: [https://imgur.com/a/ozADBCe](https://imgur.com/a/ozADBCe)
         */
        fun getSecondPreset(
            title: String? = null,
            color: Color? = null,
            cmd: String? = null,
            user: User? = null,
            fields: List<Field> = emptyList(),
            showTimestamp: Boolean = true,
            aboveTitle: String? = null,
            aboveTitleLink: String? = null,
            aboveTitleImage: String? = null
        ): KotlinEmbedBuilder {
            val builder = KotlinEmbedBuilder()
            builder.setAuthorSafe(aboveTitle, aboveTitleLink, aboveTitleImage)
            builder.setFooter(when {
                user != null && cmd != null -> "${user.asTag} • $cmd"
                user != null && cmd == null -> user.asTag
                user == null && cmd != null -> cmd
                else -> null
            })
            if (showTimestamp) {
                builder.setCurrentTimestamp()
            }
            builder.setColor(color)
            builder.title = title
            builder.fields.addAll(fields)
            return builder
        }

        fun getExtensionFunction(actions: KotlinEmbedBuilder.() -> Unit): MessageEmbed {
            return KotlinEmbedBuilder().apply(actions).build()
        }
    }

    fun setDescription(chars: CharSequence? = null): KotlinEmbedBuilder {
        description.clear()
        return appendDescription(chars)
    }

    fun appendDescription(description: CharSequence? = null): KotlinEmbedBuilder {
        description ?: return this
        Checks.check(
            this.description.length + description.length <= TEXT_MAX_LENGTH,
            "Description cannot be longer than %d characters.", TEXT_MAX_LENGTH
        )
        this.description.append(description)
        return this
    }

    fun setCurrentTimestamp(): KotlinEmbedBuilder {
        return setTimestamp(Instant.now())
    }

    fun setTimestamp(temporal: TemporalAccessor? = null): KotlinEmbedBuilder {
        when (temporal) {
            null -> timestamp = null
            is OffsetDateTime -> timestamp = temporal
            else -> {
                val offset: ZoneOffset? = try {
                    ZoneOffset.from(temporal)
                } catch (ignore: DateTimeException) {
                    ZoneOffset.UTC
                }
                timestamp = try {
                    val ldt = LocalDateTime.from(temporal)
                    OffsetDateTime.of(ldt, offset)
                } catch (ignore: DateTimeException) {
                    try {
                        val instant = Instant.from(temporal)
                        OffsetDateTime.ofInstant(instant, offset)
                    } catch (ex: DateTimeException) {
                        throw DateTimeException(
                            "Unable to obtain OffsetDateTime from TemporalAccessor: " +
                                    temporal + " of type " + temporal.javaClass.name, ex
                        )
                    }
                }
            }
        }
        return this
    }

    fun setAuthor(name: String? = null, url: String? = null, iconUrl: String? = null): KotlinEmbedBuilder {
        author = if (name == null) {
            null
        } else {
            Checks.check(
                name.length <= AUTHOR_MAX_LENGTH,
                "Name cannot be longer than %d characters.",
                AUTHOR_MAX_LENGTH
            )
            urlCheck(url)
            urlCheck(iconUrl)
            AuthorInfo(name, url, iconUrl, null)
        }
        return this
    }

    fun setAuthorSafe(name: String? = null, url: String? = null, iconUrl: String? = null): KotlinEmbedBuilder {
        kotlin.runCatching { setAuthor(name, url, iconUrl) }
        return this
    }

    fun setFooter(text: String? = null, iconUrl: String? = null): KotlinEmbedBuilder {
        footer = if (text == null) {
            null
        } else {
            Checks.check(
                text.length <= TEXT_MAX_LENGTH,
                "Text cannot be longer than %d characters.",
                TEXT_MAX_LENGTH
            )
            urlCheck(iconUrl)
            Footer(text, iconUrl, null)
        }
        return this
    }

    fun setColor(color: Color? = null): KotlinEmbedBuilder {
        this.color = color?.rgb ?: Role.DEFAULT_COLOR_RAW
        return this
    }

    fun getColor() = Color(color)

    fun setThumbnail(url: String? = null): KotlinEmbedBuilder {
        thumbnail = if (url == null) {
            null
        } else {
            urlCheck(url)
            Thumbnail(url, null, 0, 0)
        }
        return this
    }

    fun isEmpty(): Boolean {
        return title == null
                && timestamp == null
                && thumbnail == null
                && author == null
                && footer == null
                && image == null
                && color == Role.DEFAULT_COLOR_RAW
                && description.isEmpty() && fields.isEmpty()
    }

    fun getLength(): Int {
        var length = description.length
        synchronized(fields) {
            length = fields.stream()
                .map { f: Field -> (f.name?.length ?: 0) + (f.value?.length ?: 0) }
                .reduce(
                    length
                ) { a: Int, b: Int -> Integer.sum(a, b) }
        }
        length += title?.length ?: 0
        length += author?.name?.length ?: 0
        length += footer?.text?.length ?: 0
        return length
    }

    fun addField(vararg fields: Field): KotlinEmbedBuilder {
        this.fields.addAll(fields)
        return this
    }

    fun addField(name: String? = null, value: String? = null, inline: Boolean): KotlinEmbedBuilder {
        fields.add(Field(name ?: EmbedBuilder.ZERO_WIDTH_SPACE, value ?: EmbedBuilder.ZERO_WIDTH_SPACE, inline))
        return this
    }

    fun addBlankField(inline: Boolean): KotlinEmbedBuilder = addField(null, null, inline)

    private fun urlCheck(url: String? = null) {
        url ?: return
        Checks.check(
            url.length <= URL_MAX_LENGTH,
            "URL cannot be longer than %d characters.",
            URL_MAX_LENGTH
        )
        Checks.check(
            URL_PATTERN.matches(url),
            "URL must be a valid http(s) or attachment url."
        )
    }

    fun build(): MessageEmbed {
        check(!isEmpty()) { "Cannot build an empty embed!" }
        check(description.length <= TEXT_MAX_LENGTH) {
            String.format(
                "Description is longer than %d! Please limit your input!",
                TEXT_MAX_LENGTH
            )
        }
        check(getLength() <= EMBED_MAX_LENGTH_BOT) { "Cannot build an embed with more than $EMBED_MAX_LENGTH_BOT characters!" }
        val description: String? = if (description.isEmpty()) null else description.toString()
        return EntityBuilder.createMessageEmbed(
            url, title, description, EmbedType.RICH, timestamp,
            color, thumbnail, null, author, null, footer, image, LinkedList(fields)
        )
    }

    @Suppress("DuplicatedCode")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KotlinEmbedBuilder

        if (fields != other.fields) return false
        if (description.toString() != other.description.toString()) return false
        if (color != other.color) return false
        if (url != other.url) return false
        if (title != other.title) return false
        if (timestamp != other.timestamp) return false
        if (thumbnail != other.thumbnail) return false
        if (author != other.author) return false
        if (footer != other.footer) return false
        if (image != other.image) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fields.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + color
        result = 31 * result + (url?.hashCode() ?: 0)
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + (timestamp?.hashCode() ?: 0)
        result = 31 * result + (thumbnail?.hashCode() ?: 0)
        result = 31 * result + (author?.hashCode() ?: 0)
        result = 31 * result + (footer?.hashCode() ?: 0)
        result = 31 * result + (image?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "KotlinEmbedBuilder(" +
                "fields=$fields, " +
                "description=$description, " +
                "color=$color, " +
                "url=$url, " +
                "title=$title, " +
                "timestamp=$timestamp, " +
                "thumbnail=$thumbnail, " +
                "author=$author, " +
                "footer=$footer, " +
                "image=$image" +
                ")"
    }
}