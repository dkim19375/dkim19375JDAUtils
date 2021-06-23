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

import me.dkim19375.dkim19375jdautils.BotBase
import me.dkim19375.dkim19375jdautils.data.Whitelist
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl
import java.io.PrintWriter
import java.io.StringWriter
import javax.script.ScriptEngine
import javax.script.ScriptException
import kotlin.system.measureTimeMillis

private val codeBlock = Regex("```(?:(?<lang>[a-zA-Z]+)?\\n)?((?:.|\\n)*?)```")

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

    override suspend fun onCommand(
        cmd: String,
        args: List<String>,
        prefix: String,
        all: String,
        event: MessageReceivedEvent
    ) {
        val engine: ScriptEngine = GroovyScriptEngineImpl()
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
        ).plus(this.imports).joinToString("; ") { "import $it.*" }
        val code = "$imports\n${
            codeBlock.findAll(args.joinToString(" "))
                .map { it.groups.last()?.value }
                .filterNotNull()
                .firstOrNull() ?: args.joinToString(" ")
        }"

        val variables = mutableMapOf<String, Any>()
        variables["jda"] = event.jda
        variables["event"] = event
        variables["channel"] = event.channel
        variables["guild"] = event.guild
        variables["message"] = event.message
        variables["selfUser"] = event.jda.selfUser
        variables["bot"] = bot

        variables.plus(this.variables(event)).forEach(engine::put)

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
            } catch (e: ScriptException) {
                event.channel.sendMessage("ERROR: ```\n${e.localizedMessage.replace("`", "`\u200B")}```").queue()
                return
            }.replace("`", "`\u200B")
        }
        val output = strWriter.toString().replace("`", "`\u200B")
        val error = errorWriter.toString().replace("`", "`\u200B")
        event.channel.sendMessage(
            "Result: ${if (result.isEmpty()) "Nothing\n" else "```$result```"}" +
                    "Output: ${if (output.isEmpty()) "Nothing\n" else "```$output```"}" +
                    if (error.isEmpty()) "" else "Error: ```$error```" +
                            "Took ${time}ms"
        ).queue()
    }
}