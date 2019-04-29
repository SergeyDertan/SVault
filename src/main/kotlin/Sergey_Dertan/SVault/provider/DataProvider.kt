package Sergey_Dertan.SVault.provider

import Sergey_Dertan.SVault.provider.dataobject.VaultDataObject

interface DataProvider {

    fun loadVaultList(): Collection<VaultDataObject>

    fun loadVault(player: String, name: String): VaultDataObject?

    fun saveVault(vdo: VaultDataObject)

    fun saveVaultList(list: Iterable<VaultDataObject>) {
        list.forEach { saveVault(it) }
    }

    fun removeVault(player: String, name: String)

    fun getType(): Type

    fun close() {}

    enum class Type {
        YAML,
        MYSQL,
        SQLITE,
        POSTGRESQL,
        UNKNOWN;

        companion object {
            fun fromString(name: String): Type = when (name.toLowerCase()) {
                "yml", "yaml" -> YAML
                "mysql" -> MYSQL
                "sqlite", "sqlite3" -> SQLITE
                "postgresql", "postgres" -> POSTGRESQL
                else -> UNKNOWN
            }
        }
    }
}
