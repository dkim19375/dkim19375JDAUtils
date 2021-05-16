package me.dkim19375.dkim19375jdautils

import me.dkim19375.dkim19375jdautils.annotation.API
import me.dkim19375.dkim19375jdautils.command.Command
import me.dkim19375.dkim19375jdautils.command.CommandType
import me.dkim19375.dkim19375jdautils.event.CustomListener
import me.dkim19375.dkim19375jdautils.event.EventListener
import me.dkim19375.dkim19375jdautils.managers.SpecialEventsManager
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
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
abstract class BotBase {
    abstract val name: String
    abstract val token: String
    open val customListener: CustomListener = object : CustomListener() {}
    open val intents = mutableSetOf(GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.DIRECT_MESSAGE_REACTIONS)

    lateinit var jda: JDA
    val commandTypes = mutableSetOf<CommandType>()
    val commands = mutableSetOf<Command>()

    @Suppress("LeakingThis")
    @API
    val eventsManager: SpecialEventsManager = SpecialEventsManager(this)

    @API
    val consoleCommands = mutableMapOf<String, (String) -> Unit>()
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
     * @param stopCommandEnabled True if the stop console command should be enabled, false if not
     */
    @API
    fun onStart(stopCommandEnabled: Boolean = true) {
        if (started) {
            return
        }
        started = true
        println("Starting bot")
        val builder = JDABuilder.createDefault(token)
        builder.enableIntents(intents)
        val jda = builder.build()
        this.jda = jda
        jda.addEventListener(EventListener(this))
        jda.addEventListener(eventsManager)
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