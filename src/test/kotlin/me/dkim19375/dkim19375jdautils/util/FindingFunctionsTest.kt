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

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.utils.cache.SnowflakeCacheView
import net.dv8tion.jda.api.utils.concurrent.Task
import org.junit.runner.RunWith
import org.powermock.api.mockito.PowerMockito.*
import org.powermock.core.classloader.annotations.*
import org.powermock.modules.junit4.PowerMockRunner
import kotlin.test.*

private const val DUMMY = "dummy value"

@Suppress("UNCHECKED_CAST")
@RunWith(PowerMockRunner::class)
@PrepareForTest(SnowflakeCacheView::class, JDA::class)
internal class FindingFunctionsTest {

    // --------------- CACHED EMPTY ---------------

    @Test
    fun `Test empty user functions cached`() {
        val jda = mock(JDA::class.java)
        val snowflakeCacheView: SnowflakeCacheView<User> =
            mock(SnowflakeCacheView::class.java) as SnowflakeCacheView<User>

        `when`(snowflakeCacheView.asList()).thenReturn(emptyList<User>())
        `when`(jda.userCache).thenReturn(snowflakeCacheView)

        assertTrue(jda.findUsers(DUMMY).isEmpty())
    }

    @Test
    fun `Test empty emotes functions cached`() {
        val jda = mock(JDA::class.java)
        val snowflakeCacheView: SnowflakeCacheView<Emote> =
            mock(SnowflakeCacheView::class.java) as SnowflakeCacheView<Emote>

        `when`(snowflakeCacheView.asList()).thenReturn(emptyList<Emote>())
        `when`(jda.emoteCache).thenReturn(snowflakeCacheView)

        assertTrue(jda.findEmotes(DUMMY).isEmpty())
    }
    
    @Test
    fun `Test empty category functions`() {
        val jda = mock(JDA::class.java)
        val snowflakeCacheView: SnowflakeCacheView<Category> =
            mock(SnowflakeCacheView::class.java) as SnowflakeCacheView<Category>

        `when`(snowflakeCacheView.asList()).thenReturn(emptyList<Category>())
        `when`(jda.categoryCache).thenReturn(snowflakeCacheView)

        assertTrue(jda.findCategories(DUMMY).isEmpty())
    }

    @Test
    fun `Test empty text channel functions`() {
        val jda = mock(JDA::class.java)
        val snowflakeCacheView: SnowflakeCacheView<TextChannel> =
            mock(SnowflakeCacheView::class.java) as SnowflakeCacheView<TextChannel>

        `when`(snowflakeCacheView.asList()).thenReturn(emptyList<TextChannel>())
        `when`(jda.textChannelCache).thenReturn(snowflakeCacheView)

        assertTrue(jda.findTextChannels(DUMMY).isEmpty())
    }

    @Test
    fun `Test empty voice channel functions`() {
        val jda = mock(JDA::class.java)
        val snowflakeCacheView: SnowflakeCacheView<VoiceChannel> =
            mock(SnowflakeCacheView::class.java) as SnowflakeCacheView<VoiceChannel>

        `when`(snowflakeCacheView.asList()).thenReturn(emptyList<VoiceChannel>())
        `when`(jda.voiceChannelCache).thenReturn(snowflakeCacheView)

        assertTrue(jda.findVoiceChannels(DUMMY).isEmpty())
    }

    // --------------- NOT CACHED EMPTY ---------------

    @Test
    fun `Test empty user functions not cached`() {
        val jda = mock(JDA::class.java)
        val snowflakeCacheView: SnowflakeCacheView<User> =
            mock(SnowflakeCacheView::class.java) as SnowflakeCacheView<User>
        val guild = mock(Guild::class.java)
        val task = mock(Task::class.java) as Task<MutableList<Member>>
        val member = mock(Member::class.java)
        val user = mock(User::class.java)

        `when`(snowflakeCacheView.asList()).thenReturn(emptyList<User>())
        `when`(jda.guilds).thenReturn(mutableListOf(guild))
        `when`(jda.userCache).thenReturn(snowflakeCacheView)
        `when`(guild.loadMembers()).thenReturn(task)
        `when`(task.get()).thenReturn(mutableListOf(member))
        `when`(member.user).thenReturn(user)
        `when`(user.name).thenReturn("dkim19375")
        `when`(user.discriminator).thenReturn("6351")

        assertTrue(jda.findUsers(DUMMY, useCache = false).isEmpty())
    }

    @Test
    fun `Test empty emotes functions not cached`() {
        val jda = mock(JDA::class.java)
        val snowflakeCacheView: SnowflakeCacheView<Emote> =
            mock(SnowflakeCacheView::class.java) as SnowflakeCacheView<Emote>
        val guild = mock(Guild::class.java)
        val restAction = mock(RestAction::class.java) as RestAction<MutableList<ListedEmote>>
        val emote = mock(ListedEmote::class.java)

        `when`(snowflakeCacheView.asList()).thenReturn(emptyList<Emote>())
        `when`(jda.guilds).thenReturn(mutableListOf(guild))
        `when`(jda.emoteCache).thenReturn(snowflakeCacheView)
        `when`(guild.retrieveEmotes()).thenReturn(restAction)
        `when`(restAction.complete()).thenReturn(mutableListOf(emote))
        `when`(emote.name).thenReturn("slight_smile")

        assertTrue(jda.findEmotes(DUMMY, useCache = false).isEmpty())
    }
    
    // --------------- CACHED MUST NOT BE NULL ---------------

    @Test
    fun `Test user functions cached`() {
        val jda = mock(JDA::class.java)
        val snowflakeCacheView: SnowflakeCacheView<User> =
            mock(SnowflakeCacheView::class.java) as SnowflakeCacheView<User>
        val user = mock(User::class.java)

        `when`(snowflakeCacheView.asList()).thenReturn(listOf<User>(user))
        `when`(jda.userCache).thenReturn(snowflakeCacheView)
        `when`(user.name).thenReturn("dkim19375")
        `when`(user.discriminator).thenReturn("6351")

        assertEquals(jda.findUsers("dkim19375#6351").size, 1)
    }

    @Test
    fun `Test emotes functions cached`() {
        val jda = mock(JDA::class.java)
        val snowflakeCacheView: SnowflakeCacheView<Emote> =
            mock(SnowflakeCacheView::class.java) as SnowflakeCacheView<Emote>
        val emote = mock(Emote::class.java)

        `when`(snowflakeCacheView.asList()).thenReturn(listOf<Emote>(emote))
        `when`(jda.emoteCache).thenReturn(snowflakeCacheView)
        `when`(emote.name).thenReturn("slight_smile")

        assertEquals(jda.findEmotes("slight_smile").size, 1)
    }

    @Test
    fun `Test category functions`() {
        val jda = mock(JDA::class.java)
        val snowflakeCacheView: SnowflakeCacheView<Category> =
            mock(SnowflakeCacheView::class.java) as SnowflakeCacheView<Category>
        val category = mock(Category::class.java)

        `when`(snowflakeCacheView.asList()).thenReturn(listOf<Category>(category))
        `when`(jda.categoryCache).thenReturn(snowflakeCacheView)
        `when`(category.name).thenReturn("cool-category")

        assertEquals(jda.findCategories("cool-category").size, 1)
    }

    @Test
    fun `Test text channel functions`() {
        val jda = mock(JDA::class.java)
        val snowflakeCacheView: SnowflakeCacheView<TextChannel> =
            mock(SnowflakeCacheView::class.java) as SnowflakeCacheView<TextChannel>
        val channel = mock(TextChannel::class.java)

        `when`(snowflakeCacheView.asList()).thenReturn(listOf<TextChannel>(channel))
        `when`(jda.textChannelCache).thenReturn(snowflakeCacheView)
        `when`(channel.name).thenReturn("cool-channel")

        assertEquals(jda.findTextChannels("cool-channel").size, 1)
    }

    @Test
    fun `Test voice channel functions`() {
        val jda = mock(JDA::class.java)
        val snowflakeCacheView: SnowflakeCacheView<VoiceChannel> =
            mock(SnowflakeCacheView::class.java) as SnowflakeCacheView<VoiceChannel>
        val channel = mock(VoiceChannel::class.java)

        `when`(snowflakeCacheView.asList()).thenReturn(listOf<VoiceChannel>(channel))
        `when`(jda.voiceChannelCache).thenReturn(snowflakeCacheView)
        `when`(channel.name).thenReturn("cool-voice-channel")

        assertEquals(jda.findVoiceChannels("cool-voice-channel").size, 1)
    }
}