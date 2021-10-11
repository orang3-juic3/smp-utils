package me.zeddit.smputils

import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.network.protocol.game.ClientboundTabListPacket
import net.minecraft.server.MinecraftServer
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin
import java.lang.reflect.Type
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

class TabList : Listener {
    @EventHandler // wbon leave??????
    fun onPlayerJoinEvent(e: PlayerJoinEvent?) {
        HandlerList.unregisterAll(this)
        Bukkit.getScheduler().scheduleSyncRepeatingTask(JavaPlugin.getPlugin(SmpUtils::class.java),
            { updateTabList() }, 0, 4
        )
    }

    private fun updateTabList() {
        val players: List<Player> = ArrayList(Bukkit.getOnlinePlayers())
        for (i in players) {
            val tabPlayer: CraftPlayer = i as CraftPlayer
            doHeader(tabPlayer)
            doPing(tabPlayer)
        }
    }

    private fun doHeader(tabPlayer: CraftPlayer) {
        tabPlayer.handle.connection
        val conn = tabPlayer.handle.connection
        val tps: DoubleArray = MinecraftServer.getServer().recentTps
        val loc: Location = tabPlayer.location
        val builder = "§a(${loc.fmtCoord()})"
        val text = String.format(
            "{\"text\": \"%s\"}", "§2TPS : " +
                    tps[0].roundToInt() + "\\n" + builder
        )
        val footerText = "{\"text\": \"§4§lairpod shotty\"}"
        val ser = net.minecraft.network.chat.Component.Serializer()
        val context = object : JsonDeserializationContext {
            val gson  =Gson()
            override fun <T : Any?> deserialize(p0: JsonElement?, p1: Type?): T = gson.fromJson(p0,p1)
        }
        val top = ser.deserialize(JsonParser().parse(text), String::class.java, context)
        val bot = ser.deserialize(JsonParser().parse(footerText), String::class.java, context)
        val packet = ClientboundTabListPacket(top,bot)
        conn.send(packet)
    }

    private fun doPing(tabPlayer: CraftPlayer) {
        val ping: Int = tabPlayer.ping
        val colour = when {
            ping < 50 ->  NamedTextColor.GREEN
            ping < 150 ->  NamedTextColor.YELLOW
            else -> NamedTextColor.RED
        }
        val pingComp = Component.text(" ${tabPlayer.name} ${ping}ms", colour)
        tabPlayer.playerListName(pingComp)
    }
}