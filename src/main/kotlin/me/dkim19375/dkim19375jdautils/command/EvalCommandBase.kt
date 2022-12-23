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

package me.dkim19375.dkim19375jdautils.command

import dev.minn.jda.ktx.coroutines.await
import me.dkim19375.dkim19375jdautils.BotBase
import me.dkim19375.dkim19375jdautils.data.Whitelist
import me.dkim19375.dkimcore.annotation.API
import me.dkim19375.dkimcore.extension.runCatchingOrNull
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.reflect.Constructor
import javax.script.ScriptEngineFactory
import kotlin.system.measureTimeMillis


private val codeBlock = Regex("```(?:(?<lang>[a-zA-Z]+)?\\n)?((?:.|\\n)*?)```")

@API
open class EvalCommandBase(protected val bot: BotBase) : Command(bot) {
    override val aliases: Set<String> = setOf("eval")

    @Suppress("LeakingThis")
    override val arguments: Set<CommandArg> = setOf(CommandArg(this, "<code>", "The code to run"))
    override val command: String = "evaluate"
    override val description: String = "Run code for the bot!"
    override val minArgs: Int = 1
    override val name: String = "Evaluate"
    override val type: CommandType = CommandType.UTILITIES
    override val permissions: Whitelist = Whitelist(whitelist = setOf(521485088995672084L))
    open val imports: Set<String> = emptySet()
    open val variables: (MessageReceivedEvent) -> Map<String, Any> = { emptyMap() }
    private val isBukkit = runCatchingOrNull { Class.forName("org.bukkit.Bukkit") } != null
    protected open val engineType: Constructor<*> = run {
        (runCatchingOrNull { Class.forName("jdk.nashorn.api.scripting.NashornScriptEngineFactory") }
            ?: NashornScriptEngineFactory::class.java).getDeclaredConstructor()
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun onCommand(
        cmd: String,
        args: List<String>,
        prefix: String,
        all: String,
        event: MessageReceivedEvent
    ) {
        val engine = (engineType.newInstance() as ScriptEngineFactory).scriptEngine
        val imports = setOf(
            "net.dv8tion.jda.api",
            "net.dv8tion.jda.api.entities",
            "net.dv8tion.jda.api.events",
            "net.dv8tion.jda.api.managers",
            "java.io",
            "java.sql",
            "java.text",
            "java.lang",
            "java.lang.reflect",
            "java.lang.management",
            "java.math",
            "java.time",
            "java.time.chrono",
            "java.time.format",
            "java.time.temporal",
            "java.time.zone",
            "java.util",
            "java.util.regex",
            "java.util.stream",
            "java.util.logging",
            "java.util.concurrent",
            "java.util.concurrent.atomic",
        ).plus(
            if (!isBukkit) emptySet() else setOf(
                "org.bukkit",
                "org.bukkit.enchantments",
                "org.bukkit.entity",
                "org.bukkit.event",
                "org.bukkit.event.block",
                "org.bukkit.event.enchantment",
                "org.bukkit.event.entity",
                "org.bukkit.event.hanging",
                "org.bukkit.event.inventory",
                "org.bukkit.event.painting",
                "org.bukkit.event.player",
                "org.bukkit.event.server",
                "org.bukkit.event.vehicle",
                "org.bukkit.event.weather",
                "org.bukkit.event.world",
                "org.bukkit.generator",
                "org.bukkit.help",
                "org.bukkit.inventory",
                "org.bukkit.inventory.meta",
                "org.bukkit.map",
                "org.bukkit.material",
                "org.bukkit.metadata",
                "org.bukkit.plugin",
                "org.bukkit.plugin.java",
                "org.bukkit.potion",
                "org.bukkit.projectiles",
                "org.bukkit.scheduler",
                "org.bukkit.scoreboard",
                "org.spigotmc",
                "org.spigotmc.event.entity",
                "org.spigotmc.event.player"
            )
        ).plus(this.imports)
        val packages = imports.filter { import -> runCatchingOrNull { Class.forName(import) } == null }
        val importStr = "with (new JavaImporter(${
            imports.joinToString(transform = {
                "${if (it in packages) "Packages." else ""}$it"
            })
        })) { "
        val code = "$importStr${
            codeBlock.findAll(args.joinToString(" "))
                .map { it.groups.last()?.value }
                .filterNotNull()
                .firstOrNull() ?: args.joinToString(" ")
        }}"
        val variables = mutableMapOf<String, Any>()
        variables["jda"] = event.jda
        variables["event"] = event
        variables["channel"] = event.channel
        variables["guild"] = event.guild
        variables["message"] = event.message
        variables["selfUser"] = event.jda.selfUser
        variables["bot"] = bot

        variables.plus(variables(event)).filter<String?, Any?> { (key, value) ->
            key != null && value != null && key.isNotEmpty()
        }.forEach { engine.put(it.key, it.value) }

        val strWriter = StringWriter()
        val printWriter = PrintWriter(strWriter)
        engine.context.writer = printWriter

        val errorWriter = StringWriter()
        val errorPrintWriter = PrintWriter(errorWriter)
        engine.context.errorWriter = errorPrintWriter

        val result: String
        val time = measureTimeMillis {
            result = try {
                engine.eval(code)?.toString() ?: ""
            } catch (error: Throwable) {
                error.printStackTrace()
                event.channel.sendMessage("ERROR: ```\n${error.localizedMessage.replace("`", "`\u200B")}```").queue()
                return
            }.replace("`", "`\u200B")
        }
        val output = strWriter.toString().replace("`", "`\u200B")
        val error = errorWriter.toString().replace("`", "`\u200B")
        val messages = mutableListOf<String>()
        messages.addAll(splitStrSizes("Result: ", "Result: Nothing\n", result))
        messages.addAll(splitStrSizes("Output: ", "Output: Nothing\n", output))
        messages.addAll(splitStrSizes("Error: ", "", error))
        messages.add("Took ${time}ms")
        val new = messages.fold(listOf<String>()) fold@{ accumulator, element ->
            if ((accumulator.lastOrNull()?.length?.plus(element.length) ?: 2000) < 2000) {
                val new = accumulator.toMutableList()
                val value = new.removeLast()
                new.add(value + element)
                return@fold new
            } else {
                return@fold accumulator.plus(element)
            }
        }
        new.forEach {
            event.channel.sendMessage(it).await()
        }
    }

    private fun splitStrSizes(beginning: String, empty: String, str: String): List<String> {
        if (str.isEmpty()) {
            return listOf(empty)
        }
        var first = true
        return str.chunked(1990 - beginning.length) text@{
            val value = "${if (first) beginning else ""}```$it```"
            first = false
            return@text value
        }
    }
}