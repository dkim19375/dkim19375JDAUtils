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

package me.dkim19375.dkim19375jdautils.data

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.SelfUser
import net.dv8tion.jda.api.entities.User
import org.mockito.Mockito.`when`
import org.mockito.kotlin.mock
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val USER_ID: Long = 123456789012345678L
private const val SELF_USER_ID: Long = 234567890123456789L

// @Suppress("UNCHECKED_CAST")
// @RunWith(PowerMockRunner::class)
// @PrepareForTest(User::class, Member::class, GuildChannel::class, SelfUser::class, Role::class)
internal class WhitelistTest {
    private val selfUser: SelfUser = mock {
        on { isBot }.thenReturn(true)
        on { idLong }.thenReturn(SELF_USER_ID)
    }

    @Test
    fun `Test default whitelist`() {
        val jda = mock<JDA> {
            on { selfUser }.thenReturn(selfUser)
        }
        val user = mock<User> {
            on { isBot }.thenReturn(false)
            on { idLong }.thenReturn(USER_ID)
            on { it.jda }.thenReturn(jda)
        }
        val user2 = mock<User> {
            on { isBot }.thenReturn(false)
            on { idLong }.thenReturn(USER_ID)
            on { it.jda }.thenReturn(jda)
        }

        `when`(selfUser.jda).thenReturn(jda)

        assertTrue(Whitelist().hasAccess(user))
        assertTrue(Whitelist().hasAccess(user2))
        assertTrue(Whitelist().hasAccess(selfUser))
    }

    @Test
    fun `Test guild permissions`() {
        val jda = mock<JDA> {
            on { selfUser }.thenReturn(selfUser)
        }
        val user = mock<User> {
            on { isBot }.thenReturn(false)
            on { idLong }.thenReturn(USER_ID)
            on { it.jda }.thenReturn(jda)
        }
        val permissions = mutableSetOf(Permission.VIEW_CHANNEL)
        val perms2 = permissions.plus(Permission.MESSAGE_SEND)
        val member = mock<Member> {
            on { hasPermission(permissions) }.thenReturn(true)
            on { hasPermission(perms2) }.thenReturn(false)
        }

        assertTrue(Whitelist().hasAccess(user))
        assertTrue(Whitelist(permissions = permissions).hasAccess(user, member))
        assertFalse(Whitelist(perms2).hasAccess(user, member))
    }

    @Test
    fun `Test whitelist`() {
        val jda = mock<JDA> {
            on { selfUser }.thenReturn(selfUser)
        }
        val user = mock<User> {
            on { isBot }.thenReturn(false)
            on { idLong }.thenReturn(USER_ID)
            on { it.jda }.thenReturn(jda)
        }
        val permissions = mutableSetOf(Permission.VIEW_CHANNEL)

        assertTrue(Whitelist(permissions, null).hasAccess(user))
        assertTrue(Whitelist(permissions, setOf(USER_ID)).hasAccess(user))
    }

    @Test
    fun `Test blacklist`() {
        val jda = mock<JDA> {
            on { selfUser }.thenReturn(selfUser)
        }
        val user = mock<User> {
            on { isBot }.thenReturn(false)
            on { idLong }.thenReturn(USER_ID)
            on { it.jda }.thenReturn(jda)
        }
        val permissions = mutableSetOf(Permission.VIEW_CHANNEL)

        assertTrue(Whitelist(permissions, blacklist = setOf()).hasAccess(user))
        assertTrue(Whitelist(permissions, blacklist = setOf(SELF_USER_ID)).hasAccess(user))
    }

    @Test
    fun `Test ignore whitelist`() {
        val jda = mock<JDA> {
            on { selfUser }.thenReturn(selfUser)
        }
        val user = mock<User> {
            on { isBot }.thenReturn(false)
            on { idLong }.thenReturn(USER_ID)
            on { it.jda }.thenReturn(jda)
        }
        val permissions = mutableSetOf(Permission.VIEW_CHANNEL)

        assertTrue(Whitelist(permissions, null).hasAccess(user))
        assertTrue(Whitelist(permissions, setOf(USER_ID)).hasAccess(user))
        assertTrue(
            Whitelist(
                permissions,
                null,
                ignore = Pair(Whitelist.IgnoreType.IGNORE_WHITELIST, setOf(USER_ID))
            ).hasAccess(user)
        )
        assertTrue(
            Whitelist(
                permissions,
                setOf(USER_ID),
                ignore = Pair(Whitelist.IgnoreType.IGNORE_WHITELIST, setOf(USER_ID))
            ).hasAccess(user)
        )
        assertTrue(
            Whitelist(
                permissions,
                setOf(SELF_USER_ID),
                ignore = Pair(Whitelist.IgnoreType.IGNORE_WHITELIST, setOf(USER_ID))
            ).hasAccess(user)
        )
        assertFalse(Whitelist(permissions, setOf(SELF_USER_ID)).hasAccess(user))
        assertFalse(
            Whitelist(
                permissions,
                setOf(SELF_USER_ID),
                ignore = Pair(Whitelist.IgnoreType.IGNORE_WHITELIST, setOf(SELF_USER_ID))
            ).hasAccess(user)
        )
    }

    @Test
    fun `Test whitelist bots`() {
        val jda = mock<JDA> {
            on { selfUser }.thenReturn(selfUser)
        }
        val user = mock<User> {
            on { idLong }.thenReturn(USER_ID)
            on { it.jda }.thenReturn(jda)
        }
        val permissions = mutableSetOf(Permission.VIEW_CHANNEL)

        `when`(selfUser.jda).thenReturn(jda)
        assertFalse(Whitelist(permissions, setOf(), ignoreWhitelistBots = Whitelist.IgnoreType.IGNORE_WHITELIST).hasAccess(user))
        assertTrue(Whitelist(permissions, setOf(USER_ID), ignoreWhitelistBots = Whitelist.IgnoreType.IGNORE_WHITELIST).hasAccess(user))
        assertTrue(Whitelist(permissions, setOf(), ignoreWhitelistBots = Whitelist.IgnoreType.IGNORE_WHITELIST).hasAccess(selfUser))
        assertFalse(Whitelist(permissions, setOf()).hasAccess(selfUser))
        assertTrue(Whitelist(permissions, setOf(SELF_USER_ID)).hasAccess(selfUser))
    }
}