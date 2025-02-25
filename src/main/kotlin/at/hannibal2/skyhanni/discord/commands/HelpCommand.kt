package at.hannibal2.skyhanni.discord.commands

import at.hannibal2.skyhanni.discord.DiscordBot
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel

class HelpCommand : AbstractCommand() {

    override val commandName: String = "help"

    override val description: String = "Shows a help menu"

    override fun execute(args: List<String>, channel: MessageChannel, author: User, inputMessage: Message, command: String) {
        TODO("Not yet implemented")
    }

    override fun commandUsage(): String {
        val prefix = DiscordBot.prefix
        return """
            **Help Command**
            `${prefix}help` - Shows the help menu.
            `${prefix}help <command>` - Shows information about a command
        """.trimIndent()
    }

}