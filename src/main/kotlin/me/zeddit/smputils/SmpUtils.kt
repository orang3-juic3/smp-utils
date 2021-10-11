package me.zeddit.smputils

import me.zeddit.smputils.command.CoordsCommand
import me.zeddit.smputils.command.TpCommand
import me.zeddit.smputils.command.WarpCommand
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class SmpUtils : JavaPlugin() {

    companion object {
        lateinit var instance: SmpUtils
    }
    private val toClose = ArrayList<AutoCloseable>()

    override fun onEnable() {
        instance = this
        getCommand("tp")!!.apply {
            val executor = TpCommand()
            this.tabCompleter = executor
            this.setExecutor(executor)
        }
        server.pluginManager.registerEvents(TabList(), this)
        server.pluginManager.registerEvents(StackablePotionImpl(), this)
        getCommand("warp")!!.apply {
            val executor = WarpCommand()
            this.tabCompleter = executor
            this.setExecutor(executor)
            toClose.add(executor)
        }
        getCommand("coord")!!.apply {
            aliases = aliases.apply { this.add("coordc") }
            val executor = CoordsCommand("https://discord.com/api/webhooks/897166060036440144/5ZRwAmaoc_D34azqo6wFQhMjPYiR9AqPueSCpctzGfYwPXBOrpcnhED1vUycnkHEE3D3")
            this.setExecutor(executor)
            this.tabCompleter = executor
            toClose.add(executor)
        }
    }

    override fun onDisable() {
        toClose.forEach {
            it.close()
        }
        Bukkit.getLogger().info("Goodbye!")

    }
}