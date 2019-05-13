package Sergey_Dertan.SVault.event

import Sergey_Dertan.SVault.vault.VaultInventory
import cn.nukkit.event.EventHandler
import cn.nukkit.event.Listener
import cn.nukkit.event.inventory.InventoryCloseEvent
import cn.nukkit.math.Vector3

object InventoryEventHandler : Listener {

    @Suppress("UNUSED")
    @EventHandler
    fun inventoryClose(e: InventoryCloseEvent) {
        if (e.inventory is VaultInventory) {
            e.player.level.sendBlocks(arrayOf(e.player), arrayOf(e.inventory.holder as Vector3))
        }
    }
}
