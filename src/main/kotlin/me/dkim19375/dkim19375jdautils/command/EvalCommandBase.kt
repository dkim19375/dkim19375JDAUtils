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

import dev.minn.jda.ktx.await
import me.dkim19375.dkim19375jdautils.BotBase
import me.dkim19375.dkim19375jdautils.data.Whitelist
import me.dkim19375.dkimcore.annotation.API
import me.dkim19375.dkimcore.extension.runCatchingOrNull
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.io.PrintWriter
import java.io.StringWriter
import javax.script.ScriptEngineManager
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

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun onCommand(
        cmd: String,
        args: List<String>,
        prefix: String,
        all: String,
        event: MessageReceivedEvent
    ) {
        val factory = ScriptEngineManager()
        val engine = factory.getEngineByName("nashorn")
        val imports = setOf(
            "net.dv8tion.jda.api.entities.impl",
            "net.dv8tion.jda.api.managers",
            "net.dv8tion.jda.api.entities",
            "net.dv8tion.jda.api",
            "java.lang",
            "java.io",
            "java.math",
            "java.util",
            "java.util.concurrent",
            "java.time"
        ).plus(this.imports)
        val packages = imports.filter { import -> runCatchingOrNull { Class.forName(import) } == null }
        val classes = imports - packages
        val importStr = "with (new JavaImporter(${packages.joinToString(transform = { "Packages.$it" })})) { ${classes.joinToString { import ->
            "var ${import.split('.').last()} = Java.Type(\"$import\"); "
        }}"
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

        variables.plus(variables(event)).forEach(engine::put)

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