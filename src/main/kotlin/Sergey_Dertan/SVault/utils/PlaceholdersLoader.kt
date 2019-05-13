package Sergey_Dertan.SVault.utils

import Sergey_Dertan.SVault.vault.VaultManager
import com.creeperface.nukkit.placeholderapi.api.PlaceholderAPI
import java.util.function.BiFunction

//do not load this class if placeholder api not installed
object PlaceholdersLoader {

    init {
        val papi = PlaceholderAPI.getInstance()
        val api = VaultManager

        papi.visitorSensitivePlaceholder<String>("sv_list", BiFunction { p, _ -> api.getVaultList(p.name).joinToString { "$it, " } })
        papi.visitorSensitivePlaceholder<Int>("sv_amount", BiFunction { p, _ -> api.getVaultsAmount(p) })
        papi.visitorSensitivePlaceholder<String>("sv_last", BiFunction { p, _ -> api.getLastVault(p) })
    }
}
