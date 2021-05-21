# dkim19375JDAUtils
A kotlin library for JDA to help to make discord bots easier!

**WARNING! UPDATES MAY INCLUDE API BREAKING CHANGES!
THE API IS STILL VERY NEW, RESULTING IN FREQUENT CHANGES**

### An example of using this library is with my bot UniG0 - https://github.com/dkim19375/UniG0

**NOTE: This might not be up-to-date, if these code snippets don't 
work, please notify me!**

## Adding the dependency
Gradle:
```groovy
repositories {
    maven { url = 'https://jitpack.io' }
}
dependencies {
    implementation 'com.github.dkim19375:dkim19375JDAUtils:VERSION'
}
```
Maven:
```xml
<repository>
    <id>jitpack</id>
    <name>jitpack</name>
    <url>https://jitpack.io/</url>
</repository>
<dependency>
    <groupId>com.github.dkim19375</groupId>
    <artifactId>dkim19375JDAUtils</artifactId>
    <version>VERSION</version>
</dependency>
```
## Setting up the bot
A class, that holds information such as the commands, needs to be 
created. It extends `me.dkim19375.dkim19375JDAUtils.BotBase`.
To start the bot, you call BotBase#onStart.
It is recommended to make this your main class.
```kotlin
import me.dkim19375.dkim19375jdautils.BotBase

class Bot : BotBase() {
    override val name = "Bot-Name" 
    override val token = "token-here"
    
    init {
        onStart()
    }
    
    override fun getPrefix(guild: String): String {
        return "!"
    }
}
```
Now, you can simply run the bot, invite the bot to a server,
then run `!help`!