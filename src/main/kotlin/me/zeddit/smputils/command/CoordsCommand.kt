package me.zeddit.smputils.command

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class CoordsCommand : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, cmd: Command, alias: String, args: Array<out String>): Boolean {
        (sender as? Player)?.let {

        }
    }

    override fun onTabComplete(p0: CommandSender, p1: Command, p2: String, args: Array<out String>): MutableList<String> {
        return mutableListOf("village", "igloo", "ice spikes", "monument",
            "end city", "fortress", "warped forest", "bastion", "desert", "jungle", "flower forest")
    }
}