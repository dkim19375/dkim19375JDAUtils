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

package me.dkim19375.dkim19375jdautils.impl

import kotlin.test.*

private const val FIRST_VALUE = "First Value"
private const val SECOND_VALUE = "Second Value"
private const val TEST_VALUE = "Test Value"

internal class EntryImplTest {
    @Test
    fun `Test new instance values`() {
        val entry = EntryImpl(FIRST_VALUE, SECOND_VALUE)
        assertEquals(entry.key, FIRST_VALUE)
        assertEquals(entry.value, SECOND_VALUE)
        assertNotEquals(entry.key, SECOND_VALUE)
        assertNotEquals(entry.value, FIRST_VALUE)
    }

    @Test
    fun `Test return value of setValue`() {
        val entry = EntryImpl(FIRST_VALUE, SECOND_VALUE)
        assertEquals(entry.setValue(TEST_VALUE), SECOND_VALUE)
    }

    @Test
    fun `Test setting value`() {
        val entry = EntryImpl(FIRST_VALUE, SECOND_VALUE)
        assertEquals(entry.value, SECOND_VALUE)
        entry.setValue(TEST_VALUE)
        assertEquals(entry.value, TEST_VALUE)
    }
}