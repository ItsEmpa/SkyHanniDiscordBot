package at.hannibal2.skyhanni.discord.commands

import at.hannibal2.skyhanni.discord.DiscordBot
import at.hannibal2.skyhanni.discord.DiscordUtils.replyWith
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import org.reflections.Reflections
import java.lang.reflect.Modifier

object CommandHandler {

    private val commands = mutableMapOf<String, AbstractCommand>()
    private val commandAliases = mutableMapOf<String, AbstractCommand>()

    private val regex = "\\s+".toRegex()

    fun init() {
        loadCommands()
        loadAliases()
    }

    fun process(channel: MessageChannel, user: User, inputMessage: Message) {
        var msg = inputMessage.contentRaw
        if (!msg.startsWith(DiscordBot.prefix)) return
        msg = msg.substring(DiscordBot.prefix.length)
        val input = msg.split(regex, limit = 2)

        val first = input.first()
        val command = getCommand(first)

        if (command == null) {
            val tagCommand = Tags.getTag(first) ?: return
            val shouldDelete = inputMessage.referencedMessage
            inputMessage.replyWith(tagCommand)
            return
        }
        val args = input.getOrNull(1)?.split(regex).orEmpty()

        command.execute(args, channel, user, inputMessage, first)
    }

    private fun getCommand(commandName: String): AbstractCommand? {
        return commands[commandName] ?: commandAliases[commandName]
    }

    private fun loadAliases() {
        for (command in commands.values) {
            for (alias in command.aliases) {
                require(alias !in commandAliases) { "Duplicate command alias: $alias" }
                require(alias !in commands) { "Duplicate command alias: $alias" }
                commandAliases[alias] = command
            }
        }
    }

    private fun loadCommands() {
        val reflections = Reflections("at.hannibal2")
        val classes: Set<Class<out AbstractCommand>> = reflections.getSubTypesOf(AbstractCommand::class.java)
        for (clazz in classes) {
            try {
                if (Modifier.isAbstract(clazz.modifiers)) continue
                val command = clazz.getConstructor().newInstance()
                require(command.commandName !in commands) { "Duplicate command name: ${command.commandName}" }
                commands[command.commandName] = command
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}