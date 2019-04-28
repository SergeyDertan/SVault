package Sergey_Dertan.SVault.vault

import Sergey_Dertan.SVault.messenger.Messenger
import Sergey_Dertan.SVault.provider.DataProvider
import Sergey_Dertan.SVault.provider.dataobject.VaultDataObject
import Sergey_Dertan.SVault.settings.Settings
import cn.nukkit.Player
import cn.nukkit.block.BlockID
import cn.nukkit.level.GlobalBlockPalette
import cn.nukkit.math.Vector3
import cn.nukkit.nbt.NBTIO
import cn.nukkit.nbt.tag.CompoundTag
import cn.nukkit.network.protocol.BlockEntityDataPacket
import cn.nukkit.network.protocol.UpdateBlockPacket
import cn.nukkit.utils.Logger
import cn.nukkit.utils.TextFormat
import java.io.IOException
import java.nio.ByteOrder
import java.util.*

class VaultManager(private val settings: Settings, private val provider: DataProvider, logger: Logger) {

    private val vaults: MutableMap<String, MutableMap<String, VaultInventory>>

    init {
        this.vaults = TreeMap(String.CASE_INSENSITIVE_ORDER)
        var amount = 0
        for (vdo in provider.loadVaultList()) {
            val inventory = VaultInventory(VaultDataObject.fromDataObject(vdo))
            inventory.needUpdate = false
            this.vaults.computeIfAbsent(vdo.player) { TreeMap(String.CASE_INSENSITIVE_ORDER) }[vdo.name] = inventory
            ++amount
        }
        logger.info(TextFormat.GREEN.toString() + Messenger.getInstance().getMessage("loading.init.vaults-loaded", "@amount", amount.toString()))
    }

    fun hasAmountPermission(player: Player, amount: Int): Boolean {
        if (amount < this.settings.defaultMaxVaults || player.hasPermission("svault.amount.*")) return true
        for (perm in player.effectivePermissions.values) {
            if (!perm.permission.startsWith("svault.amount.*")) continue
            try {
                val am = perm.permission.replace("svault.amount.*", "").toInt()
                if (am >= amount) return true
            } catch (ignore: NumberFormatException) {
            }
        }
        return false
    }

    fun getVaultsAmount(player: Player): Int = this.vaults.getOrDefault(player.name, Collections.emptyMap()).size

    fun open(player: Player, name: String, target: String = player.name) {
        val inventory = this.vaults[target]!![name]
        val pos = sendFakeChest(player, name)
        (inventory?.holder as Vector3).setComponents(pos.x, pos.y, pos.z)
        player.addWindow(inventory)
    }

    fun createVault(player: Player, name: String) {
        this.vaults.computeIfAbsent(player.name) { TreeMap(String.CASE_INSENSITIVE_ORDER) }[name] = VaultInventory()
    }

    fun removeVault(player: String, name: String) {
        this.vaults.getOrDefault(player, Collections.emptyMap()).remove(name)
        this.provider.removeVault(player, name)
    }

    fun save(): Int {
        var saved = 0
        for ((player, vaults) in this.vaults) {
            for ((name, vault) in vaults) {
                if (vault.needUpdate) {
                    this.provider.saveVault(VaultDataObject.toDataObject(player, name, vault.contents))
                    ++saved
                    vault.needUpdate = false
                }
            }
        }
        return saved
    }

    fun vaultExists(owner: String, name: String): Boolean = this.vaults.getOrDefault(owner, Collections.emptyMap()).containsKey(name)

    fun getVaultList(player: String): Array<String> {
        val list = mutableListOf<String>()
        this.vaults.getOrDefault(player, Collections.emptyMap()).keys.forEach { list.add(it) }
        return list.toTypedArray()
    }

    private fun sendFakeChest(player: Player, name: String): Vector3 {
        val pk1 = UpdateBlockPacket()
        pk1.blockRuntimeId = GlobalBlockPalette.getOrCreateRuntimeId(BlockID.CHEST, 0)
        pk1.dataLayer = 0
        pk1.flags = UpdateBlockPacket.FLAG_NONE
        pk1.x = player.x.toInt()
        pk1.y = player.y.toInt()
        pk1.z = player.z.toInt()

        player.dataPacket(pk1)

        val pk2 = BlockEntityDataPacket()
        pk2.x = pk1.x
        pk2.y = pk1.y
        pk2.z = pk1.z

        val nbt = CompoundTag()
        nbt.putString("CustomName", Messenger.getInstance().getMessage("vault-inventory-name", "@name", name))
        try {
            pk2.namedTag = NBTIO.write(nbt, ByteOrder.LITTLE_ENDIAN, true)
        } catch (ignore: IOException) {
        }
        player.dataPacket(pk2)
        return Vector3(pk1.x.toDouble(), pk2.y.toDouble(), pk2.z.toDouble())
    }
}
