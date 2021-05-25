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

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ActionConsumer<T>(val task: () -> T) {
    companion object {
        val executor: ExecutorService = Executors.newCachedThreadPool()
    }

    fun queue(success: ((T) -> Unit) = {}, failure: ((Throwable) -> Unit) = {}) {
        executor.submit {
            val result: T
            try {
                result = task()
            } catch (error: Throwable) {
                failure(error)
                return@submit
            }
            success(result)
        }
    }

    fun complete(): T {
        return task()
    }

    fun submit(): CompletableFuture<T> {
        val future = CompletableFuture<T>()
        executor.submit future@{
            val result: T
            try {
                result = task()
            } catch (error: Throwable) {
                future.completeExceptionally(error)
                return@future
            }
            future.complete(result)
        }
        return future
    }
}