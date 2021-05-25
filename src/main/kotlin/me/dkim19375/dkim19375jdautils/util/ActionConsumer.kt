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