package Sergey_Dertan.SVault.utils

import Sergey_Dertan.SVault.vault.VaultManager
import com.creeperface.nukkit.placeholderapi.api.PlaceholderAPI

//do not load this class if placeholder api not installed
object PlaceholdersLoader {

    init {
        val papi = PlaceholderAPI.getInstance()
        val api = VaultManager

        papi.build<String>("sv_list") {
            visitorLoader {
                api.getVaultList(player.name).joinToString { "$it, " }
            }.autoUpdate(false)
        }
        papi.build<String>("sv_amount") {
            visitorLoader {
                api.getVaultsAmount(player).toString()
            }.autoUpdate(false)
        }
        papi.build<String>("sv_last") {
            visitorLoader {
                api.getLastVault(player)
            }.autoUpdate(false)
        }
    }
}
