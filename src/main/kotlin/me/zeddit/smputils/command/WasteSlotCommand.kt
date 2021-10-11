package me.zeddit.smputils.command

import me.zeddit.smputils.SmpUtils
import me.zeddit.smputils.playerOnlyRes
import me.zeddit.smputils.toError
import me.zeddit.smputils.toSuccess
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.minecraft.network.protocol.game.ClientboundSoundPacket
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.inventory.InventoryPickupItemEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*
import kotlin.collections.HashSet

class WasteSlotCommand : CommandExecutor, Listener {

    private val optedOut = HashSet<UUID>()
    private val slot : Int = 35
    private val key = NamespacedKey(SmpUtils.instance, "wasteitem")
    private val stack : ItemStack = ItemStack(Material.BARRIER).apply {
        this.itemMeta = this.itemMeta.apply {
            this.displayName(Component.text("WASTE", NamedTextColor.RED)
                .style(Style.style(TextDecoration.BOLD))) // i fucking hate these components so much
            this.persistentDataContainer.set(key, PersistentDataType.BYTE, 1)
        }
    }

    override fun onCommand(sender: CommandSender, cmd: Command, alias: String, args: Array<out String>): Boolean {
        (sender as? Player)?.let {
            return if (optedOut.contains(it.uniqueId)) {
                if (enable(it)) {
                    it.sendMessage("Turned the waste slot on!".toSuccess())
                    optedOut.remove(it.uniqueId)
                    true
                } else {
                    false
                }
            } else {
                disable(it)
                optedOut.add(it.uniqueId)
                it.sendMessage("Turned the waste slot off!".toSuccess())
                true
            }
        }
        sender.sendMessage(playerOnlyRes)
        return true
    }

    @EventHandler
    fun onInvClick(e: InventoryClickEvent) {
        e.clickedInventory?.let {
            if (it.getItem(e.slot)?.itemMeta?.persistentDataContainer?.has(key, PersistentDataType.BYTE) == true) {
                e.isCancelled = true
                e.whoClicked.setItemOnCursor(e.cursor?.let let2@{ item ->
                    if (e.isRightClick) {
                        if (item.amount-1 == 0) {
                            return@let2 null
                        } else {
                            item.amount--
                            return@let2 item
                        }
                    }
                    return@let2 null
                })
                val loc = e.whoClicked.location
                Bukkit.getPlayer(e.whoClicked.uniqueId)?.playSound(loc, Sound.BLOCK_LAVA_EXTINGUISH, 10f, 1f)
            }
        }
    }


    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        if (!enable(e.player)) {
            optedOut.add(e.player.uniqueId)
        }
    }

    @EventHandler
    fun onLeave(e: PlayerQuitEvent) {
        disable(e.player)
        optedOut.remove(e.player.uniqueId)
    }

    private fun enable(p: Player) : Boolean {
        val inv = p.inventory
        clean(inv)
        return if (inv.getItem(slot) == null) {
            inv.setItem(slot, stack)
            true
        } else {
            p.sendMessage("The waste slot is occupied, please make sure there is nothing in it!".toError())
            false
        }
    }

    private fun disable(p:Player) {
        clean(p.inventory)
        optedOut.add(p.uniqueId)
    }

    private fun clean(inv: Inventory) {
        inv.forEachIndexed pogs@{ i, itemStack ->
            if (itemStack== null) return@pogs
            itemStack.itemMeta?.let {
                if (it.persistentDataContainer.has(key, PersistentDataType.BYTE)) {
                    inv.setItem(i, null)
                }
            }
        }
    }

}