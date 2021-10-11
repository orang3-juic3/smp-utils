package me.zeddit.smputils.command

import me.zeddit.smputils.toError
import me.zeddit.smputils.toSuccess
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.world.entity.monster.Zombie
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.entity.Vex

class TpCommand : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("Only players can execute this command!".toError())
            return true
        }
        if (args.size != 1) {
            sender.sendMessage("Incorrect args supplied!".toError())
            return true
        }
        Bukkit.getPlayer(args[0])?.let {
            if (it == sender) {
                sender.sendMessage("You can't tp to yourself, dumbass.".toError())
                return true
            }
            sender.teleport(it)
            sender.sendMessage("Teleported you to ${it.name}!".toSuccess())
            it.sendMessage("${sender.name} just teleported to you!".toSuccess())
            return true
        }
        sender.sendMessage(Component.text("Could not find player ${sender.name}!", NamedTextColor.RED))
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command,
                               alias: String, args: Array<out String>): MutableList<String> {
        if (args.size != 1) {
            return emptyList<String>().toMutableList()
        }
        return Bukkit.getOnlinePlayers()
            .map { it.name }
            .filter { it.startsWith(args[0], true) }
            .toMutableList()

    }
}