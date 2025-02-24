package at.hannibal2.skyhanni.discord

import at.hannibal2.skyhanni.discord.commands.CommandHandler
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent
import java.util.Scanner

object DiscordBot : ListenerAdapter() {
    private lateinit var config: BotConfig

    fun setConfig(config: BotConfig): DiscordBot {
        this.config = config
        return this
    }

    init {
        CommandHandler.init()
    }

    val prefix: String get() = config.prefix
    val botCommandChannelId: String get() = config.botCommandChannelId

    private val regex = "\\s+".toRegex()

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.guild.id != config.allowedServerId) return

        val message = event.message.contentRaw.trim()
        val args = message.split(" ", limit = 3)

        if (event.author.isBot) return

        CommandHandler.process(event.channel, event.author, event.message)

        fun logAction(action: String) {
            val author = event.author
            val name = author.name
            val effectiveName = author.effectiveName
            val globalName = author.globalName
            println("$effectiveName ($name/$globalName) $action")
        }

        fun reply(message: String) {
            event.message.reply(message).queue()
        }

        if (message.startsWith("!")) {
            var keyword = message.substring(1)
            var silent = false
            if (keyword.endsWith(" -s")) {
                keyword = keyword.dropLast(3)
                silent = true
            }
            val response = Database.getResponse(keyword)
            if (response != null) {
                if (silent) {
                    logAction("used silent keyword '$keyword'")
                    event.message.delete().queue {
                        event.channel.sendMessage(response).queue()
                    }
                } else {
                    event.message.referencedMessage?.let {
                        it.reply(response).queue()
                        event.message.delete().queue()
                    } ?: run {
                        reply(response)
                    }
                }
                return
            }
        }

        if (message == "!taglist" || message == "!list") {
            val keywords = Database.listKeywords().joinToString(", ")
            val response = if (keywords.isNotEmpty()) "📌 Keywords: $keywords" else "No keywords set."
            reply(response)
            return
        }

        // checking that only staff can change tags
        if (event.channel.id != config.botCommandChannelId) return

        when {
            args[0] == "!add" && args.size == 3 -> {
                val keyword = args[1]
                val response = args[2]
                if (Database.listKeywords().contains(keyword.lowercase())) {
                    reply("❌ Already exists use `!edit` instead.")
                    return
                }
                if (Database.addKeyword(keyword, response)) {
                    reply("✅ Keyword '$keyword' added!")
                    logAction("added '$keyword' with response `$response`")
                } else {
                    reply("❌ Failed to add keyword.")
                }
                return
            }

            args[0] == "!edit" && args.size == 3 -> {
                val keyword = args[1]
                val response = args[2]
                if (!Database.listKeywords().contains(keyword.lowercase())) {
                    reply("❌ Keyword does not exist! Use `!add` instead.")
                    return
                }
                if (Database.addKeyword(keyword, response)) {
                    reply("✅ Keyword '$keyword' edited!")
                    logAction("edited '$keyword' with response `$response`")
                } else {
                    reply("❌ Failed to edit keyword.")
                }
                return
            }

            args[0] == "!delete" && args.size == 2 -> {
                val keyword = args[1]
                if (Database.deleteKeyword(keyword)) {
                    reply("🗑️ Keyword '$keyword' deleted!")
                    logAction("deleted '$keyword'")
                } else {
                    reply("❌ Keyword '$keyword' not found.")
                }
                return
            }

            message == "!help" -> {
                val commands = listOf("add", "remove", "taglist", "edit")
                val response = "The bot currently supports these commands: ${commands.joinToString(", ", prefix = "!")}"
                reply(response)
                return
            }
        }

        if (message.startsWith("!")) {
            val s = "Unknown command \uD83E\uDD7A Type `!help` for help."
            reply(s)
        }
    }
}

fun main() {
    val config = ConfigLoader.load("config.json")
    val token = config.token

    val jda = with(JDABuilder.createDefault(token)) {
        addEventListeners(DiscordBot.setConfig(config))
        enableIntents(GatewayIntent.MESSAGE_CONTENT)
    }.build()
    jda.awaitReady()

    fun sendMessageToBotChannel(message: String) {
        jda.getTextChannelById(config.botCommandChannelId)?.sendMessage(message)?.queue()
    }

    sendMessageToBotChannel("I'm awake \uD83D\uDE42")

    Thread {
        val scanner = Scanner(System.`in`)
        while (scanner.hasNextLine()) {
            val input = scanner.nextLine().trim().lowercase()
            if (input in listOf("close", "stop", "exit", "end")) {
                sendMessageToBotChannel("Manually shutting down \uD83D\uDC4B")
                jda.shutdown()
                break
            }
        }
    }.start()

    Runtime.getRuntime().addShutdownHook(Thread {
        sendMessageToBotChannel("I am the shutdown hook and I say bye \uD83D\uDC4B")
    })
}
