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

import dev.minn.jda.ktx.injectKTX
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.dkim19375.dkim19375jdautils.command.Command
import me.dkim19375.dkim19375jdautils.command.CommandType
import me.dkim19375.dkim19375jdautils.command.HelpCommand
import me.dkim19375.dkim19375jdautils.command.OTHER_TYPE
import me.dkim19375.dkim19375jdautils.event.CustomListener
import me.dkim19375.dkim19375jdautils.event.EventListener
import me.dkim19375.dkim19375jdautils.impl.CustomJDABuilder
import me.dkim19375.dkim19375jdautils.manager.SpecialEventsManager
import me.dkim19375.dkimcore.annotation.API
import me.dkim19375.dkimcore.file.YamlFile
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.requests.GatewayIntent
import java.util.*
import kotlin.concurrent.thread
import kotlin.system.exitProcess

val SCOPE = CoroutineScope(Dispatchers.Default)

/**
 * Bot base
 *
 * @constructor Create a bot base, should be the main class of your bot
 */
@API
@Suppress("LeakingThis")
abstract class BotBase {
    private val ioScope = CoroutineScope(Dispatchers.IO)
    /**
     * The name of the bot
     */
    abstract val name: String

    /**
     * The token of the bot
     */
    abstract val token: String

    /**
     * Whether to Inject KTS from JDA-KTS
     */
    open val injectKTS = false

    /**
     * The base JDA builder
     */
    open val baseJDABuilder: (Unit) -> CustomJDABuilder = builder@{
        val builder = CustomJDABuilder.createDefault(token)
        if (injectKTS) {
            builder.actions.add { it.injectKTX() }
        }
        return@builder builder.enableIntents(intents)
            .addEventListeners(eventsManager, EventListener(this))
    }

    /**
     * Allows you to run methods on [CustomJDABuilder], overriding the ones ran by the [BotBase#onStart][BotBase.onStart]
     */
    open val jdaBuilderActions: ((CustomJDABuilder) -> CustomJDABuilder)? = null

    /**
     * The listener to detect if a command is valid to send
     */
    open val customListener: CustomListener = object : CustomListener() {}

    /**
     * [GatewayIntents][GatewayIntent] that should be enabled
     */
    @API
    val intents = mutableSetOf(GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.DIRECT_MESSAGE_REACTIONS)

    /**
     * The [JDA] instance
     */
    lateinit var jda: JDA

    /**
     * [CommandTypes][me.dkim19375.dkim19375jdautils.command.CommandType] that should be registered
     */
    open val commandTypes = mutableSetOf(OTHER_TYPE)

    /**
     * [Commands][Command] that should be registered
     */
    open val commands = mutableSetOf<Command>(HelpCommand(this))

    /**
     * A [Set] of [YamlFiles][YamlFile] to register
     */
    open val files = mutableSetOf<YamlFile>()

    /**
     * An [Events Manager][SpecialEventsManager] useful for handling events such as reaction add listeners
     */
    @API
    open val eventsManager: SpecialEventsManager = SpecialEventsManager(this)

    /**
     * Console commands that should be triggered.
     *
     * The key of the map: The base command
     *
     * The value of the map: The raw string/input
     */
    @API
    open val consoleCommands = mutableMapOf<String, suspend (String) -> Unit>()

    /**
     * True if the bot is started, false if not
     */
    private var started = false

    /**
     * @param guild The guild to get the prefix of
     * @return The prefix
     */
    fun getPrefix(guild: Long?): String = getPrefix(guild?.toString())

    /**
     * @param guild The guild to get the prefix of
     * @return The prefix
     */
    abstract fun getPrefix(guild: String?): String

    /**
     * Get all command types
     *
     * @return All [CommandTypes][CommandType] from [commands]
     */
    open fun getAllCommandTypes(): Set<CommandType> {
        val types = commandTypes.toMutableSet()
        types.addAll(commands.map(Command::type))
        return types
    }

    /**
     * @param stopCommandEnabled True if the stop console command should be enabled, false if not
     */
    @API
    fun onStart(stopCommandEnabled: Boolean = true) {
        if (started) {
            return
        }
        started = true
        println("Starting bot")
        val builder = jdaBuilderActions?.let { it(baseJDABuilder(Unit)) } ?: baseJDABuilder(Unit)
        jda = builder.getBuilder().build()
        Runtime.getRuntime().addShutdownHook(thread(false) {
            if (jda.status != JDA.Status.SHUTDOWN && jda.status != JDA.Status.SHUTTING_DOWN) {
                println("Stopping the bot!")
                jda.shutdown()
                println("Stopped")
            }
        })
        ioScope.launch {
            val scanner = Scanner(System.`in`)
            while (scanner.hasNext()) {
                val next = scanner.nextLine()
                if (next.equals("stop", ignoreCase = true) && stopCommandEnabled) {
                    if (jda.status != JDA.Status.SHUTDOWN && jda.status != JDA.Status.SHUTTING_DOWN) {
                        println("Stopping the bot!")
                        jda.shutdown()
                        println("Stopped")
                        exitProcess(0)
                    }
                    continue
                }
                for ((cmd, action) in consoleCommands) {
                    if (next.startsWith(cmd, ignoreCase = true)) {
                        SCOPE.launch {
                            action(next)
                        }
                    }
                }
            }
        }
    }

    @API
    open fun reloadFiles() = files.forEach(YamlFile::reload)

    @API
    open fun saveFiles() = files.forEach(YamlFile::save)

    /**
     * The method which is called them a command is being sent to all of the [Command]s in [commands]
     *
     * @param event the event of the command
     */
    open fun sendEvent(event: (Command) -> Unit) = commands.forEach(event)
}