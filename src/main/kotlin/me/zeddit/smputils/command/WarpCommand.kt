package me.zeddit.smputils.command

import me.zeddit.smputils.SmpUtils
import me.zeddit.smputils.toError
import me.zeddit.smputils.toSuccess
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.command.*
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.lang.Exception
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class WarpCommand: CommandExecutor, TabCompleter, AutoCloseable {


    private val file = File(SmpUtils.instance.dataFolder, "warps.yml")
    private val config = YamlConfiguration()
    private val thread = Executors.newSingleThreadExecutor()
    private val maxWarps = 3


    init {
        thread.submit {
            Thread.currentThread().name = "Warp Command IO Thread"
            Bukkit.getLogger().info("Warp Command IO Thread started!")
        }
        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.createNewFile()
        }
        config.load(file)
    }


    // follows the crud model (read is in the command impl)

    private enum class SubCommand {
        CREATE,
        UPDATE,
        DELETE,
        RELOAD,
        LIST;
        override fun toString() : String = this.name.toLowerCase()
        companion object {
            fun fromString(str: String) : SubCommand? = values().firstOrNull {str == it.toString()}
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, alias: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage("Not enough args supplied!".toError())
            return true
        }
        val arg0 = args[0].toLowerCase()
        val cmd = SubCommand.fromString(arg0)
        when {
            cmd == SubCommand.RELOAD -> {
                if (!sender.hasPermission("smp.warp.reload")) {
                    sender.sendMessage("You are not authenticated enough to perform this command!".toError())
                    return true
                }
                try {
                    config.load(file)
                } catch (e: Exception) {
                    sender.sendMessage("There was a problem reloading the config. Check logs for details.".toError())
                    return true
                }
                sender.sendMessage("Successfully reloaded config!".toSuccess())
                return true
            }
            sender !is Player -> {
                sender.sendMessage("You need to be a player in order to execute this command!".toError())
                return true
            }

            cmd != null -> {
                when (cmd) {
                    SubCommand.LIST -> {
                        val playerSection = config.getConfigurationSection("${sender.uniqueId}")
                        val noWarps = "You have no warps! Create one with /warp create".toError()
                        if (playerSection == null) {
                            sender.sendMessage(noWarps)
                            return false
                        }
                        val keys = playerSection.getKeys(false)
                        if (keys.isEmpty()) {
                            sender.sendMessage(noWarps)
                            return false
                        }
                        sender.sendMessage(Component.text("Your warps:\n", NamedTextColor.BLUE)
                            .appendSafe(ArrayList(keys).apply { removeLast() })
                            .append(Component.text(keys.last(), NamedTextColor.BLUE)))
                        return true
                    }
                    SubCommand.UPDATE, SubCommand.DELETE, SubCommand.CREATE -> {
                        val playerSection = config.getConfigurationSection("${sender.uniqueId}") ?: config.createSection("${sender.uniqueId}")
                        if (args.size != 2) {
                            sender.sendMessage("Invalid args supplied!".toError())
                            return true
                        }
                        val warp = playerSection.getConfigurationSection(args[1].toLowerCase())
                        val doesntExist = "Warp '${args[1].toLowerCase()}' does not exist!".toError()
                        when (cmd) {
                            SubCommand.CREATE -> {
                                if (warp != null) {
                                    updateLoc(sender, warp)
                                } else {
                                    if (playerSection.getKeys(false).size >= maxWarps) {
                                        sender.sendMessage("You cannot create any more warps!".toError())
                                        return false
                                    }
                                    if (EnumSet.allOf(SubCommand::class.java).map { it.toString() }.contains(args[1].toLowerCase())) {
                                        sender.sendMessage("You cannot create a warp with this name, as it is a reserved keyword!".toError())
                                        return false
                                    }
                                    val section = playerSection.createSection(args[1].toLowerCase())
                                    sender.location.serialize().forEach { (k, v) ->
                                        section[k] = v
                                    }
                                    sender.sendMessage("Created new warp with name ${section.name}!".toSuccess())
                                }
                            }
                            SubCommand.DELETE -> {
                                if (warp == null) {
                                    sender.sendMessage(doesntExist)
                                    return false
                                }
                                config.set(warp.currentPath!!, null)
                                sender.sendMessage("Successfully deleted warp ${args[1].toLowerCase()}!".toSuccess())
                            }
                            SubCommand.UPDATE -> {
                                if (warp == null) {
                                    sender.sendMessage(doesntExist)
                                    return false
                                }
                                updateLoc(sender, warp)
                            }
                            else -> throw IllegalStateException("???")
                        }
                        thread.submit {
                            save()
                        }
                        return true
                    }
                    else -> throw IllegalStateException("???")
                }
            }
            else -> {
                val playerSection = config.getConfigurationSection("${sender.uniqueId}")
                playerSection?.let { section ->
                    val warp = section.getConfigurationSection(arg0)
                    warp?.let { warpNn ->
                        val loc = Location.deserialize(warpNn.getKeys(false).associateWith { warp[it] })
                        sender.teleport(loc)
                        loc.world.playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f)
                        sender.sendMessage("Warped you to $arg0!".toSuccess())
                        return true
                    }
                }
                sender.sendMessage("Could not find warp ${arg0}!".toError())
                return false
            }
        }
    }

    private fun Component.appendSafe(keys: MutableList<String>) : Component {
        if (keys.isEmpty()) {
            return this
        }
        return keys.map { Component.text("$it\n", NamedTextColor.BLUE) }
            .reduce { acc, current -> acc.append(current) }
    }

    private fun updateLoc(player: Player, warp: ConfigurationSection) {
        player.location.serialize().forEach { (k, v) ->
            warp[k] = v
        }
        player.sendMessage("Updated location for warp ${warp.name}!".toSuccess())
    }

    @Synchronized
    fun save() {
        config.save(file)
    }

    override fun onTabComplete(sender: CommandSender, cmd: Command, alias: String,args: Array<out String>): MutableList<String>? {
        val ops = if (args.size< 2) EnumSet.allOf(SubCommand::class.java).map { it.toString() } else EnumSet.noneOf(SubCommand::class.java).map { it.toString() }
        if (sender !is Player) return null
        config.getConfigurationSection("${sender.uniqueId}")?.let { playerConfig ->
            return playerConfig.getKeys(false).toMutableList().apply {addAll(ops)}
        }
        return ops.toMutableList()
    }
    override fun close() {
        thread.shutdown()
        thread.awaitTermination(2, TimeUnit.MINUTES)
    }
}