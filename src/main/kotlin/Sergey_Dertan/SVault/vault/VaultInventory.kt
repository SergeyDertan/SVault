package Sergey_Dertan.SVault.vault

import cn.nukkit.inventory.CustomInventory
import cn.nukkit.inventory.Inventory
import cn.nukkit.inventory.InventoryHolder
import cn.nukkit.inventory.InventoryType
import cn.nukkit.item.Item
import cn.nukkit.math.Vector3

class VaultInventory(items: Map<Int, Item> = mutableMapOf(), holder: Holder = Holder(0.0, 0.0, 0.0)) : CustomInventory(holder, InventoryType.CHEST, items) {

    var needUpdate = false

    override fun setItem(index: Int, item: Item?, send: Boolean): Boolean {
        val result = super.setItem(index, item, send)
        if (result) needUpdate = true
        return result
    }

    class Holder(x: Double, y: Double, z: Double) : Vector3(x, y, z), InventoryHolder {
        override fun getInventory(): Inventory? = null
    }
}
