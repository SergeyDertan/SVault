package Sergey_Dertan.SVault.settings

import Sergey_Dertan.SVault.main.SVaultMain
import Sergey_Dertan.SVault.main.SVaultMain.Companion.MAIN_FOLDER
import Sergey_Dertan.SVault.provider.DataProvider
import Sergey_Dertan.SVault.utils.Utils.copyResource
import cn.nukkit.utils.Config
import cn.nukkit.utils.MainLogger

class Settings {

    val updateNotifier: Boolean
    val defaultMaxVaults: Int
    val provider: DataProvider.Type

    val autoSave: Boolean
    val autoSavePeriod: Int

    init {
        copyResource("config.yml", "resources/", MAIN_FOLDER, SVaultMain::class.java)

        val cnf = this.getConfig()

        this.defaultMaxVaults = (cnf["default-max-vaults"] as Number).toInt()
        this.updateNotifier = cnf["update-notifier"] as Boolean
        this.provider = DataProvider.Type.fromString(cnf["provider"] as String)

        this.autoSave = cnf["auto-save"] as Boolean
        this.autoSavePeriod = cnf["auto-save-period"] as Int
    }

    @Suppress("WEAKER_ACCESS")
    fun getConfig(): Map<String, Any> {
        return Config(MAIN_FOLDER + "config.yml", Config.YAML).all
    }
}
