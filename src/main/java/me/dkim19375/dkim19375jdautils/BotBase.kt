package me.dkim19375.dkim19375jdautils

import me.dkim19375.dkim19375jdautils.annotation.API
import me.dkim19375.dkim19375jdautils.command.Command
import me.dkim19375.dkim19375jdautils.command.CommandType
import me.dkim19375.dkim19375jdautils.event.CustomListener
import me.dkim19375.dkim19375jdautils.event.EventListener
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import java.util.*
import kotlin.concurrent.thread
import kotlin.system.exitProcess

@API
abstract class BotBase(
    val name: String,
    @API val token: String,
    val customListener: CustomListener = object : CustomListener() {}
) {
    @API
    lateinit var jda: JDA
    val commandTypes = mutableSetOf<CommandType>()

    @API
    val commands = mutableSetOf<Command>()

    @API
    val consoleCommands = mutableMapOf<String, (String) -> Unit>()
    private var started = false

    fun getPrefix(guild: Long): String = getPrefix(guild.toString())
    abstract fun getPrefix(guild: String): String

    @API
    fun onStart(stopCommandEnabled: Boolean = true) {
        if (started) {
            return
        }
        started = true
        println("Starting bot")
        val builder = JDABuilder.createDefault(token)
        val jda = builder.build()
        this.jda = jda
        jda.addEventListener(EventListener(this))
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

    fun sendEvent(event: (Command) -> Unit) = commands.forEach(event)
}