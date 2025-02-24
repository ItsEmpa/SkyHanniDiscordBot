package at.hannibal2.skyhanni.discord.commands

import at.hannibal2.skyhanni.discord.DiscordUtils.equalsOneOf
import at.hannibal2.skyhanni.discord.DiscordUtils.error
import at.hannibal2.skyhanni.discord.DiscordUtils.replyWith
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel

class TagCommand : AbstractCommand() {

    override val commandName = "tag"

    override val aliases: List<String> = listOf("dtag")

    override val description: String = "Manage tags"

    override fun execute(args: List<String>, channel: MessageChannel, author: User, inputMessage: Message, command: String) {
        if (args.isEmpty()) return inputMessage.replyWith(commandUsage())

        val first = args.first()

        when (first) {
            "add" -> {
                if (args.size < 3) return inputMessage.wrongUsage("add <tag> <content>")
                val tagName = args.first()
                if (Tags.containsTag(tagName)) return inputMessage.error("Tag already exists.")
                val response = args.drop(2).joinToString(" ")
                if (Tags.addTag(tagName, response)) {
                    inputMessage.replyWith("Tag added")
                } else {
                    inputMessage.error("Failed to add tag")
                }
                return
            }
            "delete", "remove" -> {
                val tagName = args.first()
                if (!Tags.containsTag(tagName)) return inputMessage.error("Tag does not exist.")
                if (Tags.deleteTag(tagName)) {
                    inputMessage.replyWith("Tag deleted")
                } else {
                    inputMessage.error("Failed to delete tag")
                }
                return
            }
            "edit" -> {
                if (args.size < 3) return inputMessage.wrongUsage("edit <tag> <content>")
                val tagName = args.first()
                if (!Tags.containsTag(tagName)) return inputMessage.error("Tag does not exist.")
                val response = args.drop(2).joinToString(" ")
                if (Tags.editTag(tagName, response)) {
                    inputMessage.replyWith("Tag edited")
                } else {
                    inputMessage.error("Failed to edit tag")
                }
                return
            }
            "list" -> {
                val tags = Tags.listTags()
                if (tags.isEmpty()) {
                    inputMessage.replyWith("No tags found")
                } else {
                    inputMessage.replyWith("📌 Tags: ${tags.joinToString(", ")}")
                }
                return
            }
            else -> {
                val tag = Tags.getTag(first)
                if (tag != null) {
                    val shouldDelete = command.equals("dtag", ignoreCase = true) || args.getOrNull(1).equalsOneOf("-d", "-s")
                    Tags.replyTo(channel, inputMessage, shouldDelete, tag)
                    return
                }
            }
        }
    }

    override fun commandUsage(): String {
        return """
            **Tag Command**
            `!tag add <tag> <content>` - Add a new tag
            `!tag delete <tag>` - Delete a tag
            `!tag edit <tag> <content>` - Edit a tag
            `!tag list` - List all tags
            `!tag <tag>` - Get a tag
        """.trimIndent()
    }


}