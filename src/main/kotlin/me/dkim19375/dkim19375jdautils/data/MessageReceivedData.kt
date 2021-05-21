package me.dkim19375.dkim19375jdautils.data

import me.dkim19375.dkim19375jdautils.annotation.API

@API
class MessageReceivedData(
    val command: String,
    val args: List<String>,
    val prefix: String,
    val all: String
)