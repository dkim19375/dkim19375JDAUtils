package me.dkim19375.dkim19375jdautils.command

import net.dv8tion.jda.api.Permission

data class CommandArg(val baseCommand: Command, val arg: String, val description: String, val permissions: Set<Permission> = setOf())