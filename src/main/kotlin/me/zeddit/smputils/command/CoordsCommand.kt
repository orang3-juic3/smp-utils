package me.zeddit.smputils.command

import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.WebhookClientBuilder
import club.minnced.discord.webhook.send.WebhookEmbed
import me.zeddit.smputils.fmtCoord
import me.zeddit.smputils.toError
import me.zeddit.smputils.toSuccess
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.awt.Color
import java.time.Instant
import java.time.ZoneOffset

class CoordsCommand(webhookUrl : String) : CommandExecutor, TabCompleter, AutoCloseable {

    private val client: WebhookClient

    init {
        val builder = WebhookClientBuilder(webhookUrl)
        builder.setThreadFactory {
            val t = Thread(it)
            t.name = "Webhook thread"
            t
        }
        client = builder.setWait(false).build()
    }

    override fun onCommand(sender: CommandSender, cmd: Command, alias: String, args: Array<out String>): Boolean {
        (sender as? Player)?.let {
            val loc = it.location
            if (alias == "coordc") {
                sender.chat(loc.fmtCoord())
                return true
            }
            val name = sender.name
            val desc = let {
                val join = args.joinToString("")
                join.ifEmpty {
                    "No description provided."
                }
            }
            val field = WebhookEmbed.EmbedField(false, loc.fmtCoord(), desc)
            client.send(WebhookEmbed(Instant.now().atOffset(ZoneOffset.UTC), Color.GREEN.rgb,
                null, null, null, null, WebhookEmbed.EmbedTitle("Coords", null),
                WebhookEmbed.EmbedAuthor(name, null, "https://namemc.com/search?q=$name"), listOf(field)))
            sender.sendMessage("Sent your coords to discord!".toSuccess())
            return true
        }
        sender.sendMessage("You have to be a player in order to use this command!".toError())
        return true
    }

    override fun onTabComplete(p0: CommandSender, p1: Command, p2: String, args: Array<out String>): MutableList<String> {
        return mutableListOf("village", "igloo", "ice spikes", "monument",
            "end city", "fortress", "warped forest", "bastion", "desert", "jungle", "flower forest")
    }

    override fun close() {
        client.close()
    }
}