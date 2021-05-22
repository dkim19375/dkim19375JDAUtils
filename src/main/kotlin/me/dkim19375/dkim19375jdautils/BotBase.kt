package me.dkim19375.dkim19375jdautils

import dev.minn.jda.ktx.injectKTX
import me.dkim19375.dkim19375jdautils.annotation.API
import me.dkim19375.dkim19375jdautils.command.Command
import me.dkim19375.dkim19375jdautils.command.CommandType
import me.dkim19375.dkim19375jdautils.command.HelpCommand
import me.dkim19375.dkim19375jdautils.command.OTHER_TYPE
import me.dkim19375.dkim19375jdautils.event.CustomListener
import me.dkim19375.dkim19375jdautils.event.EventListener
import me.dkim19375.dkim19375jdautils.impl.CustomJDABuilder
import me.dkim19375.dkim19375jdautils.managers.SpecialEventsManager
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.requests.GatewayIntent
import java.util.*
import kotlin.concurrent.thread
import kotlin.system.exitProcess

/**
 * Bot base
 *
 * @constructor Create a bot base, should be the main class of your bot
 */
@API
@Suppress("LeakingThis")
abstract class BotBase {
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
    open val consoleCommands = mutableMapOf<String, (String) -> Unit>()

    /**
     * True if the bot is started, false if not
     */
    private var started = false

    /**
     * @param guild The guild to get the prefix of
     * @return The prefix
     */
    fun getPrefix(guild: Long): String = getPrefix(guild.toString())

    /**
     * @param guild The guild to get the prefix of
     * @return The prefix
     */
    abstract fun getPrefix(guild: String): String

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
        thread {
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
                        action(next)
                    }
                }
            }
        }
    }

    /**
     * The method which is called them a command is being sent to all of the [Command]s in [commands]
     *
     * @param event the event of the command
     */
    open fun sendEvent(event: (Command) -> Unit) = commands.forEach(event)
}