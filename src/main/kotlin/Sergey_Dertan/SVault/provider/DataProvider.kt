package Sergey_Dertan.SVault.provider

import Sergey_Dertan.SVault.provider.dataobject.VaultDataObject

interface DataProvider {

    fun loadVaultList(): List<VaultDataObject>

    fun loadVault(player: String, name: String): VaultDataObject

    fun saveVault(vdo: VaultDataObject)

    fun saveVaultList(list: Iterable<VaultDataObject>) {
        list.forEach { saveVault(it) }
    }

    fun removeVault(player: String, name: String)

    fun getType(): Type

    enum class Type {
        YAML,
        UNKNOWN;

        companion object {
            fun fromString(name: String): Type = when (name.toLowerCase()) {
                "yml", "yaml" -> YAML
                else -> UNKNOWN
            }
        }
    }
}