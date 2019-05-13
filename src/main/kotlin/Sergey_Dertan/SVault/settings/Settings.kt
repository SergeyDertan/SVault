package Sergey_Dertan.SVault.settings

import Sergey_Dertan.SVault.main.SVaultMain
import Sergey_Dertan.SVault.main.SVaultMain.Companion.DB_FOLDER
import Sergey_Dertan.SVault.main.SVaultMain.Companion.MAIN_FOLDER
import Sergey_Dertan.SVault.provider.DataProvider
import Sergey_Dertan.SVault.utils.Utils.copyResource
import cn.nukkit.utils.Config

object Settings {

    val updateNotifier: Boolean
    val defaultMaxVaults: Int
    val provider: DataProvider.Type

    val autoSave: Boolean
    val autoSavePeriod: Int

    val mySQLSettings: MySQLSettings
    val sqliteSettings: SQLiteSettings
    val postgreSQLSettings: PostgreSQLSettings

    init {
        copyResource("config.yml", "resources/", MAIN_FOLDER, SVaultMain::class.java)
        copyResource("mysql.yml", "resources/db", DB_FOLDER, SVaultMain::class.java)
        copyResource("postgresql.yml", "resources/db", DB_FOLDER, SVaultMain::class.java)
        copyResource("sqlite.yml", "resources/db", DB_FOLDER, SVaultMain::class.java)

        this.mySQLSettings = MySQLSettings(Config(DB_FOLDER + "mysql.yml", Config.YAML).all)
        this.sqliteSettings = SQLiteSettings(Config(DB_FOLDER + "sqlite.yml", Config.YAML)["database-file"] as String)
        this.postgreSQLSettings = PostgreSQLSettings(Config(DB_FOLDER + "postgresql.yml", Config.YAML).all)

        val cnf = this.getConfig()

        this.defaultMaxVaults = (cnf["default-max-vaults"] as Number).toInt()
        this.updateNotifier = cnf["update-notifier"] as Boolean
        this.provider = DataProvider.Type.fromString(cnf["provider"] as String)

        this.autoSave = cnf["auto-save"] as Boolean
        this.autoSavePeriod = cnf["auto-save-period"] as Int
    }

    @Suppress("WEAKER_ACCESS")
    fun getConfig(): Map<String, Any> = Config(MAIN_FOLDER + "config.yml", Config.YAML).all
}
