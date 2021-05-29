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
import org.mockito.kotlin.*
import kotlin.test.*

private const val DUMMY = "dummy value"

@Suppress("UNCHECKED_CAST")
// @RunWith(PowerMockRunner::class)
// @PrepareForTest(SnowflakeCacheView::class, JDA::class)
internal class FindingFunctionsTest {

    // --------------- CACHED EMPTY ---------------

    @Test
    fun `Test empty user functions cached`() {
        val snowflakeCacheView = mock<SnowflakeCacheView<User>> {
            on { asList() }.thenReturn(emptyList<User>())            
        }
        val jda = mock<JDA> {
            on { userCache }.thenReturn(snowflakeCacheView)
        }
        assertTrue(jda.findUsersBlocking(DUMMY).isEmpty())
    }

    @Test
    fun `Test empty emotes functions cached`() {
        val snowflakeCacheView = mock<SnowflakeCacheView<Emote>> {
            on { asList() }.thenReturn(emptyList<Emote>())            
        }
        val jda = mock<JDA> {
            on { emoteCache }.thenReturn(snowflakeCacheView)
        }
        assertTrue(jda.findEmotesBlocking(DUMMY).isEmpty())
    }

    @Test
    fun `Test empty category functions`() {
        val snowflakeCacheView = mock<SnowflakeCacheView<Category>> {
            on { asList() }.thenReturn(emptyList<Category>())
        }
        val jda = mock<JDA> {
            on { categoryCache }.thenReturn(snowflakeCacheView)
        }

        assertTrue(jda.findCategories(DUMMY).isEmpty())
    }

    @Test
    fun `Test empty text channel functions`() {
        val snowflakeCacheView = mock<SnowflakeCacheView<TextChannel>> {
            on { asList() }.thenReturn(emptyList<TextChannel>())
        }
        val jda = mock<JDA> {
            on { textChannelCache }.thenReturn(snowflakeCacheView)
        }

        assertTrue(jda.findTextChannels(DUMMY).isEmpty())
    }

    @Test
    fun `Test empty voice channel functions`() {
        val snowflakeCacheView = mock<SnowflakeCacheView<VoiceChannel>> {
            on { asList() }.thenReturn(emptyList<VoiceChannel>())
        }
        val jda = mock<JDA> {
            on { voiceChannelCache }.thenReturn(snowflakeCacheView)
        }

        assertTrue(jda.findVoiceChannels(DUMMY).isEmpty())
    }

    // --------------- NOT CACHED EMPTY ---------------

    @Test
    fun `Test empty user functions not cached`() {
        val snowflakeCacheView = mock<SnowflakeCacheView<User>> {
            on { asList() }.thenReturn(emptyList<User>())
        }
        val user = mock<User> {
            on { name }.thenReturn("dkim19375")
            on { discriminator }.thenReturn("6351")
        }
        val member = mock<Member> {
            on { it.user }.thenReturn(user)
        }
        val task = mock<Task<MutableList<Member>>> {
            on { get() }.thenReturn(mutableListOf(member))
        }
        val guild = mock<Guild> {
            on { loadMembers() }.thenReturn(task)
        }
        val jda = mock<JDA> {
            on { guilds }.thenReturn(mutableListOf(guild))
            on { userCache }.thenReturn(snowflakeCacheView)
        }

        assertTrue(jda.findUsersBlocking(DUMMY, useCache = false).isEmpty())
    }

    @Test
    fun `Test empty emotes functions not cached`() {
        val snowflakeCacheView = mock<SnowflakeCacheView<Emote>> {
            on { asList() }.thenReturn(emptyList<Emote>())
        }
        val emote = mock<ListedEmote> {
            on { name }.thenReturn("slight_smile")
        }
        val restAction = mock<RestAction<MutableList<ListedEmote>>> {
            on { complete() }.thenReturn(mutableListOf(emote))
        }
        val guild = mock<Guild> {
            on { retrieveEmotes() }.thenReturn(restAction)
        }
        val jda = mock<JDA> {
            on { guilds }.thenReturn(mutableListOf(guild))
            on { emoteCache }.thenReturn(snowflakeCacheView)
        }

        assertTrue(jda.findEmotesBlocking(DUMMY, useCache = false).isEmpty())
    }

    // --------------- CACHED MUST NOT BE NULL ---------------

    @Test
    fun `Test user functions cached`() {
        val user = mock<User> {
            on { name }.thenReturn("dkim19375")
            on { discriminator }.thenReturn("6351")
        }
        val snowflakeCacheView = mock<SnowflakeCacheView<User>> {
            on { asList() }.thenReturn(listOf(user))
        }
        val jda = mock<JDA> {
            on { userCache }.thenReturn(snowflakeCacheView)
        }

        assertEquals(jda.findUsersBlocking("dkim19375#6351").size, 1)
    }

    @Test
    fun `Test emotes functions cached`() {
        val emote = mock<Emote> {
            on { name }.thenReturn("slight_smile")
        }
        val snowflakeCacheView = mock<SnowflakeCacheView<Emote>> {
            on { asList() }.thenReturn(listOf(emote))
        }
        val jda = mock<JDA> {
            on { emoteCache }.thenReturn(snowflakeCacheView)
        }

        assertEquals(jda.findEmotesBlocking("slight_smile").size, 1)
    }

    @Test
    fun `Test category functions`() {
        val category = mock<Category> {
            on { name }.thenReturn("cool-category")
        }
        val snowflakeCacheView = mock<SnowflakeCacheView<Category>> {
            on { asList() }.thenReturn(listOf(category))
        }
        val jda = mock<JDA> {
            on { categoryCache }.thenReturn(snowflakeCacheView)
        }

        assertEquals(jda.findCategories("cool-category").size, 1)
    }

    @Test
    fun `Test text channel functions`() {
        val channel = mock<TextChannel> {
            on { name }.thenReturn("cool-channel")
        }
        val snowflakeCacheView = mock<SnowflakeCacheView<TextChannel>> {
            on { asList() }.thenReturn(listOf(channel))
        }
        val jda = mock<JDA> {
            on { textChannelCache }.thenReturn(snowflakeCacheView)
        }

        assertEquals(jda.findTextChannels("cool-channel").size, 1)
    }

    @Test
    fun `Test voice channel functions`() {
        val channel = mock<VoiceChannel> {
            on { name }.thenReturn("cool-voice-channel")
        }
        val snowflakeCacheView = mock<SnowflakeCacheView<VoiceChannel>> {
            on { asList() }.thenReturn(listOf(channel))
        }
        val jda = mock<JDA> {
            on { voiceChannelCache }.thenReturn(snowflakeCacheView)
        }

        assertEquals(jda.findVoiceChannels("cool-voice-channel").size, 1)
    }
}