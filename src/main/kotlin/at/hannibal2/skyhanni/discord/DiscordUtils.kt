package at.hannibal2.skyhanni.discord

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel

object DiscordUtils {

    fun Message.replyWith(message: String) = this.reply(message).queue()

    fun Message.error(message: String) = this.reply("**Error**: $message").queue()

    fun MessageChannel.send(message: String) = this.sendMessage(message).queue()

    fun <T> T.equalsOneOf(vararg values: T): Boolean = values.any { it == this }

    fun sendMessageToBotChannel(message: String) {
        DiscordBot.jda.getTextChannelById(DiscordBot.botCommandChannelId)?.send(message)
    }

}