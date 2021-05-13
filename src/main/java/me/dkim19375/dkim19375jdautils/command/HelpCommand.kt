package me.dkim19375.dkim19375jdautils.command

import me.dkim19375.dkim19375jdautils.BotBase
import me.dkim19375.dkim19375jdautils.annotation.API
import me.dkim19375.dkim19375jdautils.embed.EmbedManager
import me.dkim19375.dkim19375jdautils.embed.EmbedUtils
import me.dkim19375.dkim19375jdautils.util.getCommand
import me.dkim19375.dkim19375jdautils.util.getOfType
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import java.awt.Color

@API
class HelpCommand(private val bot: BotBase) : Command(bot) {
    override val command = "help"
    override val name = "Help"
    override val aliases = setOf<String>()
    override val description = "See the bot's commands"
    override val arguments: Set<CommandArg>
        get() = bot.commandTypes.map { type ->
            CommandArg(
                this,
                type.displayname.lowercase(),
                "View commands in the ${type.displayname.lowercase()} category"
            )
        }.toSet()
    override val type = object : CommandType("OTHER", "Other") {}
    override val minArgs = 1

    override fun onGuildCommand(
        cmd: String,
        args: List<String>,
        prefix: String,
        all: String,
        event: GuildMessageReceivedEvent
    ) {
        val type = args[0].getCommandType(bot)
        if (type == null) {
            val command = args[0].getCommand(bot)
            if (command == null) {
                sendHelpUsage(cmd, event)
                return
            }
            sendHelpUsage(cmd, event, command)
            return
        }
        val embedManager = EmbedManager("UniG0 $name: ${type.displayname}", Color.BLUE, cmd, event.author)
        embedManager.embedBuilder.addField(
            "TIP:", "Do ${bot.getPrefix(event.guild.id)}help <command> " +
                    "to view information about a specific command!", false
        )
        embedManager.embedBuilder.addField("Information:", "Commands in the ${type.displayname} category", false)
        embedManager.embedBuilder.addField(
            EmbedUtils.getEmbedGroup("Commands - ${type.displayname}:", bot.commands.getOfType(type).map { c ->
                "${c.command} - ${c.description}"
            })
        )
        event.channel.sendMessage(embedManager.embedBuilder.build()).queue()
    }
}