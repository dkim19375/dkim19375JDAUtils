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

import com.neovisionaries.ws.client.WebSocketFactory
import net.dv8tion.jda.api.GatewayEncoding
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.audio.factory.IAudioSendFactory
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.hooks.IEventManager
import net.dv8tion.jda.api.hooks.VoiceDispatchInterceptor
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.Compression
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.SessionController
import net.dv8tion.jda.api.utils.cache.CacheFlag
import okhttp3.OkHttpClient
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.ScheduledExecutorService
import javax.annotation.CheckReturnValue

@Suppress("unused", "MemberVisibilityCanBePrivate")
class CustomJDABuilder(val creation: (Unit) -> JDABuilder) {
    val actions = mutableListOf<(JDABuilder) -> JDABuilder>()

    companion object {
        @CheckReturnValue
        fun createDefault(token: String): CustomJDABuilder = CustomJDABuilder { JDABuilder.createDefault(token) }

        @CheckReturnValue
        fun createDefault(
            token: String,
            intent: GatewayIntent,
            vararg intents: GatewayIntent
        ): CustomJDABuilder = CustomJDABuilder { JDABuilder.createDefault(token, intent, *intents) }

        @CheckReturnValue
        fun createDefault(token: String, intents: Collection<GatewayIntent>): CustomJDABuilder =
            CustomJDABuilder { JDABuilder.createDefault(token, intents) }

        @CheckReturnValue
        fun createLight(token: String): CustomJDABuilder = CustomJDABuilder { JDABuilder.createLight(token) }

        @CheckReturnValue
        fun createLight(
            token: String,
            intent: GatewayIntent,
            vararg intents: GatewayIntent
        ): CustomJDABuilder = CustomJDABuilder {
            JDABuilder.createLight(token, intent, *intents)
        }

        @CheckReturnValue
        fun createLight(token: String, intents: Collection<GatewayIntent>): CustomJDABuilder =
            CustomJDABuilder { JDABuilder.createLight(token, intents) }

        @CheckReturnValue
        fun create(intent: GatewayIntent, vararg intents: GatewayIntent): CustomJDABuilder =
            CustomJDABuilder { JDABuilder.create(intent, *intents) }

        @CheckReturnValue
        fun create(intents: Collection<GatewayIntent>): CustomJDABuilder =
            CustomJDABuilder { JDABuilder.create(intents) }

        @CheckReturnValue
        fun create(token: String?, intent: GatewayIntent, vararg intents: GatewayIntent): CustomJDABuilder =
            CustomJDABuilder { JDABuilder.create(token, intent, *intents) }

        @CheckReturnValue
        fun create(token: String?, intents: Collection<GatewayIntent>): CustomJDABuilder =
            CustomJDABuilder { JDABuilder.create(token, intents) }
    }

    fun setGatewayEncoding(encoding: GatewayEncoding): CustomJDABuilder {
        actions.add { it.setGatewayEncoding(encoding) }
        return this
    }

    fun setRawEventsEnabled(enable: Boolean): CustomJDABuilder {
        actions.add { it.setRawEventsEnabled(enable) }
        return this
    }

    fun setRelativeRateLimit(enable: Boolean): CustomJDABuilder {
        actions.add { it.setRelativeRateLimit(enable) }
        return this
    }

    fun enableCache(flags: Collection<CacheFlag>): CustomJDABuilder {
        actions.add { it.disableCache(flags) }
        return this
    }

    fun enableCache(flag: CacheFlag, vararg flags: CacheFlag): CustomJDABuilder {
        actions.add { it.enableCache(flag, *flags) }
        return this
    }

    fun disableCache(flags: Collection<CacheFlag>): CustomJDABuilder {
        actions.add { it.disableCache(flags) }
        return this
    }

    fun disableCache(flag: CacheFlag, vararg flags: CacheFlag): CustomJDABuilder {
        actions.add { it.disableCache(flag, *flags) }
        return this
    }

    fun setMemberCachePolicy(policy: MemberCachePolicy?): CustomJDABuilder {
        actions.add { it.setMemberCachePolicy(policy) }
        return this
    }

    fun setContextMap(map: ConcurrentMap<String, String>?): CustomJDABuilder {
        actions.add { it.setContextMap(map) }
        return this
    }

    fun setContextEnabled(enable: Boolean): CustomJDABuilder {
        actions.add { it.setContextEnabled(enable) }
        return this
    }

    fun setCompression(compression: Compression): CustomJDABuilder {
        actions.add { it.setCompression(compression) }
        return this
    }

    fun setRequestTimeoutRetry(retryOnTimeout: Boolean): CustomJDABuilder {
        actions.add { it.setRequestTimeoutRetry(retryOnTimeout) }
        return this
    }

    fun setToken(token: String?): CustomJDABuilder {
        actions.add { it.setToken(token) }
        return this
    }

    fun setHttpClientBuilder(builder: OkHttpClient.Builder?): CustomJDABuilder {
        actions.add { it.setHttpClientBuilder(builder) }
        return this
    }

    fun setHttpClient(client: OkHttpClient?): CustomJDABuilder {
        actions.add { it.setHttpClient(client) }
        return this
    }

    fun setWebsocketFactory(factory: WebSocketFactory?): CustomJDABuilder {
        actions.add { it.setWebsocketFactory(factory) }
        return this
    }

    fun setRateLimitPool(pool: ScheduledExecutorService?): CustomJDABuilder {
        actions.add { it.setRateLimitPool(pool) }
        return this
    }

    fun setRateLimitPool(pool: ScheduledExecutorService?, automaticShutdown: Boolean): CustomJDABuilder {
        actions.add { it.setRateLimitPool(pool, automaticShutdown) }
        return this
    }

    fun setGatewayPool(pool: ScheduledExecutorService?): CustomJDABuilder {
        actions.add { it.setGatewayPool(pool) }
        return this
    }

    fun setGatewayPool(pool: ScheduledExecutorService?, automaticShutdown: Boolean): CustomJDABuilder {
        actions.add { it.setGatewayPool(pool, automaticShutdown) }
        return this
    }

    fun setCallbackPool(executor: ExecutorService?): CustomJDABuilder {
        actions.add { it.setCallbackPool(executor) }
        return this
    }

    fun setCallbackPool(executor: ExecutorService, automaticShutdown: Boolean): CustomJDABuilder {
        actions.add { it.setCallbackPool(executor, automaticShutdown) }
        return this
    }

    fun setEventPool(executor: ExecutorService?): CustomJDABuilder {
        actions.add { it.setEventPool(executor) }
        return this
    }

    fun setEventPool(executor: ExecutorService?, automaticShutdown: Boolean): CustomJDABuilder {
        actions.add { it.setEventPool(executor, automaticShutdown) }
        return this
    }

    fun setAudioPool(pool: ScheduledExecutorService?): CustomJDABuilder {
        actions.add { it.setAudioPool(pool) }
        return this
    }

    fun setAudioPool(pool: ScheduledExecutorService?, automaticShutdown: Boolean): CustomJDABuilder {
        actions.add { it.setAudioPool(pool, automaticShutdown) }
        return this
    }

    fun setBulkDeleteSplittingEnabled(enabled: Boolean): CustomJDABuilder {
        actions.add { it.setBulkDeleteSplittingEnabled(enabled) }
        return this
    }

    fun setEnableShutdownHook(enable: Boolean): CustomJDABuilder {
        actions.add { it.setEnableShutdownHook(enable) }
        return this
    }

    fun setAutoReconnect(autoReconnect: Boolean): CustomJDABuilder {
        actions.add { it.setAutoReconnect(autoReconnect) }
        return this
    }

    fun setEventManager(manager: IEventManager?): CustomJDABuilder {
        actions.add { it.setEventManager(manager) }
        return this
    }

    fun setAudioSendFactory(factory: IAudioSendFactory?): CustomJDABuilder {
        actions.add { it.setAudioSendFactory(factory) }
        return this
    }

    fun setIdle(idle: Boolean): CustomJDABuilder {
        actions.add { it.setIdle(idle) }
        return this
    }

    fun setActivity(activity: Activity?): CustomJDABuilder {
        actions.add { it.setActivity(activity) }
        return this
    }

    fun setStatus(status: OnlineStatus): CustomJDABuilder {
        actions.add { it.setStatus(status) }
        return this
    }

    fun addEventListeners(vararg listeners: Any): CustomJDABuilder {
        actions.add { it.addEventListeners(*listeners) }
        return this
    }

    fun removeEventListeners(vararg listeners: Any): CustomJDABuilder {
        actions.add { it.removeEventListeners(*listeners) }
        return this
    }

    fun setMaxReconnectDelay(maxReconnectDelay: Int): CustomJDABuilder {
        actions.add { it.setMaxReconnectDelay(maxReconnectDelay) }
        return this
    }

    fun useSharding(shardId: Int, shardTotal: Int): CustomJDABuilder {
        actions.add { it.useSharding(shardId, shardTotal) }
        return this
    }

    fun setSessionController(controller: SessionController?): CustomJDABuilder {
        actions.add { it.setSessionController(controller) }
        return this
    }

    fun setVoiceDispatchInterceptor(interceptor: VoiceDispatchInterceptor?): CustomJDABuilder {
        actions.add { it.setVoiceDispatchInterceptor(interceptor) }
        return this
    }

    fun setChunkingFilter(filter: ChunkingFilter?): CustomJDABuilder {
        actions.add { it.setChunkingFilter(filter) }
        return this
    }

    fun setDisabledIntents(intent: GatewayIntent, vararg intents: GatewayIntent): CustomJDABuilder {
        actions.add { it.setDisabledIntents(intent, *intents) }
        return this
    }

    fun setDisabledIntents(intents: Collection<GatewayIntent>?): CustomJDABuilder {
        actions.add { it.setDisabledIntents(intents) }
        return this
    }

    fun disableIntents(intents: Collection<GatewayIntent>): CustomJDABuilder {
        actions.add { it.disableIntents(intents) }
        return this
    }

    fun disableIntents(intent: GatewayIntent, vararg intents: GatewayIntent): CustomJDABuilder {
        actions.add { it.disableIntents(intent, *intents) }
        return this
    }

    fun setEnabledIntents(intent: GatewayIntent, vararg intents: GatewayIntent): CustomJDABuilder {
        actions.add { it.setEnabledIntents(intent, *intents) }
        return this
    }

    fun setEnabledIntents(intents: Collection<GatewayIntent>?): CustomJDABuilder {
        actions.add { it.setEnabledIntents(intents) }
        return this
    }

    fun enableIntents(intents: Collection<GatewayIntent>): CustomJDABuilder {
        actions.add { it.enableIntents(intents) }
        return this
    }

    fun enableIntents(intent: GatewayIntent, vararg intents: GatewayIntent): CustomJDABuilder {
        actions.add { it.enableIntents(intent, *intents) }
        return this
    }

    fun setLargeThreshold(threshold: Int): CustomJDABuilder {
        actions.add { it.setLargeThreshold(threshold) }
        return this
    }

    fun setMaxBufferSize(bufferSize: Int): CustomJDABuilder {
        actions.add { it.setMaxBufferSize(bufferSize) }
        return this
    }

    fun getBuilder(): JDABuilder {
        val builder = creation(Unit)
        actions.forEach { it(builder) }
        return builder
    }
}