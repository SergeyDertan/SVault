package Sergey_Dertan.SVault.provider.dataobject

import cn.nukkit.item.Item
import cn.nukkit.nbt.NBTIO
import com.google.gson.Gson
import java.nio.ByteOrder
import javax.jdo.annotations.PersistenceCapable
import javax.jdo.annotations.Persistent

@PersistenceCapable(table = "svvaults", detachable = "true")
class VaultDataObject(@Persistent(name = "player") val player: String, @Persistent(name = "name") val name: String, @Persistent(name = "items") val items: String) {

    companion object {
        fun fromDataObject(vdo: VaultDataObject): Map<Int, Item> {
            val items = mutableMapOf<Int, Item>()

            @Suppress("UNCHECKED_CAST")
            for ((invId, itemData) in Gson().fromJson(vdo.items, Map::class.java) as Map<String, Map<String, Any>>) {
                val id = (itemData["id"] as Number).toInt()
                val meta = (itemData["meta"] as Number).toInt()

                val item = Item.get(id, meta)
                itemData["nbt"]?.let {
                    val nbt = NBTIO.read(it as ByteArray, ByteOrder.LITTLE_ENDIAN)
                    item.namedTag = nbt
                }
                items[invId.toInt()] = item
            }
            return items
        }

        fun toDataObject(player: String, name: String, items: Map<Int, Item>): VaultDataObject {
            val seri = mutableMapOf<Int, MutableMap<String, Any>>()

            for ((id, item) in items) {
                val itemData = mutableMapOf<String, Any>()
                itemData["id"] = item.id
                itemData["meta"] = item.damage
                item.namedTag?.let {
                    itemData["nbt"] = NBTIO.write(it, ByteOrder.LITTLE_ENDIAN)
                }
                seri[id] = itemData
            }
            return VaultDataObject(player, name, Gson().toJson(seri))
        }
    }
}