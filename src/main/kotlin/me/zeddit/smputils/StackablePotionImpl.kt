package me.zeddit.smputils

import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.*
import org.bukkit.inventory.ItemStack

class StackablePotionImpl : Listener {

    private fun event(e: Iterable<ItemStack?>) {
        /*if (e.filterNotNull().none {
                when (it.type) {
                    Material.LINGERING_POTION -> true
                    Material.POTION -> true
                    Material.SPLASH_POTION -> true
                    else -> false
                }
            }) return*/
        setMatStackType()
        setMatStackItem()
    }
    private fun setMatStackType() {
        setVal(Material.SPLASH_POTION)
        setVal(Material.LINGERING_POTION)
        setVal(Material.POTION)
    }

    private fun setVal(obj: Any) {
        obj::class.java.getDeclaredField("maxStack").apply {
            this.isAccessible = true
            this.set(obj, 3)
        }
    }

    private fun setMatStackItem() {
        val iField = Item::class.java.getDeclaredField("maxStackSize")
        iField.isAccessible = true
        iField.set(Items.POTION, 3)
        iField.set(Items.SPLASH_POTION, 3)
        iField.set(Items.LINGERING_POTION, 3)
    }

    @EventHandler
    fun onInventoryClickEvent(e: InventoryClickEvent) {
        /*event(e.inventory)
        if (e.isShiftClick) return
        if (e.cursor == null) return
        if (when (e.cursor!!.type) {
                Material.POTION -> false
                Material.SPLASH_POTION -> false
                Material.LINGERING_POTION -> false
                else -> true
        }) return
        val inv = e.whoClicked.inventory.toList()
        val toAdd =inv[e.slot]
        if (!e.cursor!!.isSimilar(toAdd)) return
        e.isCancelled = true
        if (e.isRightClick && e.action == InventoryAction.PLACE_ONE) {
            if (toAdd.amount == toAdd.maxStackSize) {
                return
            }
            toAdd.amount++
            if (e.cursor!!.amount == 1) {
                e.whoClicked.setItemOnCursor(null)
            } else {
                e.cursor!!.amount--
            }
        } else {
            if (e.action ==InventoryAction.PLACE_ALL) {
                val over = toAdd.amount + e.cursor!!.amount
                if (over - 64 > 0) {
                    toAdd.amount = 64
                    e.cursor!!.amount = over -64
                } else {
                    e.whoClicked.setItemOnCursor(null)
                    toAdd.amount = over
                }
            }
        }*/
    }
    @EventHandler
    fun onInventoryCloseEvent(e: InventoryCloseEvent) {
        event(e.inventory)
    }
    @EventHandler
    fun onInventoryCreativeEvent(e: InventoryCreativeEvent) {
        event(e.inventory)
    }
    @EventHandler
    fun onInventoryDragEvent(e: InventoryDragEvent) {
        event(e.inventory)
    }
    @EventHandler
    fun onInventoryInteractEvent(e: InventoryInteractEvent) {
        event(e.inventory)
    }
    @EventHandler
    fun onInventoryMoveItemEvent(e: InventoryMoveItemEvent) {
        event(e.source)
        event(e.destination)
    }
    @EventHandler
    fun onEvent(e: InventoryInteractEvent) {
        event(e.inventory)
    }
    @EventHandler
    fun onInventoryPickupItemEvent(e: InventoryPickupItemEvent) {
        event(e.inventory)
    }
}
