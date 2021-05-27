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

package me.dkim19375.dkim19375jdautils

import me.dkim19375.dkim19375jdautils.data.MessageReceivedData
import me.dkim19375.dkim19375jdautils.util.EventType
import kotlin.test.*

/**
 * Coverage tests - used for mostly data classes so that the coverage isn't that low :P
 * (Classes that don't have anything to test)
 */
internal class CoverageTests {
    @Test
    fun `Test MessageReceivedData class`() {
        val data = MessageReceivedData("a", listOf("b"), "c", "d")
        assertEquals(data.command, "a")
        assertContentEquals(data.args, listOf("b"))
        assertEquals(data.prefix, "c")
        assertEquals(data.all, "d")
    }

    @Test
    fun `Test EventType enum`() {
        assertEquals(EventType.PRIVATE, EventType.PRIVATE)
        assertEquals(EventType.GUILD, EventType.GUILD)
        assertEquals(EventType.GENERIC, EventType.GENERIC)
    }
}