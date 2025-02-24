package at.hannibal2.skyhanni.discord.commands

import at.hannibal2.skyhanni.discord.Database
import at.hannibal2.skyhanni.discord.DiscordUtils.replyWith
import at.hannibal2.skyhanni.discord.DiscordUtils.send
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel

object Tags {

    /** Returns the response of the tag. */
    fun getTag(tag: String): String? = Database.getResponse(tag)

    /** Returns true if the tag is succesfully added. */
    fun addTag(tag: String, response: String): Boolean = Database.addKeyword(tag, response)

    /** Returns true if the tag is succesfully deleted. */
    fun deleteTag(tag: String): Boolean = Database.deleteKeyword(tag)

    /** Returns true if the tag is succesfully updated. */
    fun editTag(tag: String, response: String): Boolean = Database.addKeyword(tag, response)

    /** Returns a list of all tags. */
    fun listTags(): List<String> = Database.listKeywords()

    /** Returns true if the tag exists. */
    fun containsTag(tag: String): Boolean = Database.containsKeyword(tag)

    fun replyTo(channel: MessageChannel, message: Message, shouldDelete: Boolean, response: String) {
        val referenced = message.referencedMessage
        if (shouldDelete) {
            message.delete().queue()

            // we check this here because the only instance where we should send a message without replying
            // to another one is when we delete the original message and don't reply to anyone else
            if (referenced == null) {
                channel.send(response)
                return
            }
        }

        val replyMessage = referenced ?: message
        replyMessage.replyWith(response)
    }

}