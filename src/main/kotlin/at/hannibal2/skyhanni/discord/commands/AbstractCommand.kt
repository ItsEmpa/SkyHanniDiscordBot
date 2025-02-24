package at.hannibal2.skyhanni.discord.commands

import at.hannibal2.skyhanni.discord.DiscordBot
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel

abstract class AbstractCommand {

    abstract val commandName: String

    open val aliases: List<String> = listOf()

    abstract val description: String

    abstract fun commandUsage(): String

    abstract fun execute(args: List<String>, channel: MessageChannel, author: User, inputMessage: Message, command: String)

    protected fun Message.wrongUsage(usage: String) = this.reply("**Wrong usage**: `${DiscordBot.prefix}$commandName $usage`").queue()

}