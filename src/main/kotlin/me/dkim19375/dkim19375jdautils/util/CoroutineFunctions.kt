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

import net.dv8tion.jda.api.requests.RestAction
import java.util.concurrent.Future
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun <T> RestAction<T>.await(
    failure: (Continuation<T>, Throwable) -> Unit = { continuation, throwable ->
        continuation.resumeWithException(throwable)
    },
): T = suspendCoroutine { cont ->
    this.queue({ cont.resume(it) }, { failure(cont, it) })
}

suspend fun <T> Future<T>.await(
    failure: (Continuation<T>, Throwable) -> Unit = { continuation, throwable ->
        continuation.resumeWithException(throwable)
    }
): T = await(failure) { get() }

suspend fun <T> await(
    failure: (Continuation<T>, Throwable) -> Unit = { continuation, throwable ->
        continuation.resumeWithException(throwable)
    },
    action: (() -> T)
): T = suspendCoroutine { cont ->
    try {
        cont.resume(action())
    } catch (e: Throwable) {
        failure(cont, e)
    }
}