package Sergey_Dertan.SVault.main

import Sergey_Dertan.SVault.command.*
import Sergey_Dertan.SVault.event.InventoryEventHandler
import Sergey_Dertan.SVault.event.NotifierEventHandler
import Sergey_Dertan.SVault.messenger.Messenger
import Sergey_Dertan.SVault.provider.DataProvider
import Sergey_Dertan.SVault.provider.YAMLDataProvider
import Sergey_Dertan.SVault.provider.database.MySQLDataProvider
import Sergey_Dertan.SVault.provider.database.PostgreSQLDataProvider
import Sergey_Dertan.SVault.provider.database.SQLiteDataProvider
import Sergey_Dertan.SVault.settings.Settings
import Sergey_Dertan.SVault.utils.DataProviderException
import Sergey_Dertan.SVault.utils.PlaceholdersLoader
import Sergey_Dertan.SVault.utils.Utils.compareVersions
import Sergey_Dertan.SVault.utils.Utils.httpGetRequestJson
import Sergey_Dertan.SVault.vault.VaultManager
import cn.nukkit.Server
import cn.nukkit.command.Command
import cn.nukkit.plugin.LibraryLoadException
import cn.nukkit.plugin.LibraryLoader
import cn.nukkit.plugin.PluginBase
import cn.nukkit.utils.TextFormat
import cn.nukkit.utils.Utils
import java.io.File

@Suppress("WEAKER_ACCESS")
class SVaultMain : PluginBase() {

    private var forceShutdown = false

    lateinit var messenger: Messenger
        private set
    lateinit var settings: Settings
        private set
    lateinit var vaultManager: VaultManager
        private set
    lateinit var mainCommand: VaultCommand
        private set
    lateinit var provider: DataProvider
        private set
    /**
     * <version, description>
     */
    lateinit var newVersion: Pair<String, String>
        private set

    companion object {
        @JvmStatic
        val MAIN_FOLDER = Server.getInstance().dataPath + "Sergey_Dertan_Plugins/SVault/"
        @JvmStatic
        val LANG_FOLDER = MAIN_FOLDER + "Lang/"
        @JvmStatic
        val VAULTS_FOLDER = MAIN_FOLDER + "Vaults/"
        @JvmStatic
        val DB_FOLDER = MAIN_FOLDER + "DB/"
        const val VERSION_URL = "https://api.github.com/repos/SergeyDertan/SVault/releases/latest"

        @JvmStatic
        lateinit var instance: SVaultMain
            private set
    }

    override fun onEnable() {
        if (!this.createDirectories()) return
        if (!this.initMessenger()) return

        if (!this.loadLibraries()) return

        if (!this.initSettings()) return

        if (!this.initDataProvider()) return

        instance = this

        this.initVaults()

        this.initEventHandler()

        this.initCommands()

        this.initAutoSave()

        this.loadPlaceholders()

        this.server.scheduler.scheduleTask(this, { this.checkUpdate() }, true)
    }


    @Suppress("WEAKER_ACCESS")
    fun registerCommand(command: Command) {
        this.mainCommand.registerCommand(command)
    }

    fun getProviderInstance(type: DataProvider.Type): DataProvider {
        if (::provider.isInitialized && this.provider.getType() == type) return this.provider
        try {
            when (type) {
                DataProvider.Type.YAML -> return YAMLDataProvider()
                DataProvider.Type.SQLITE -> {
                    this.loadSQLiteLibraries()
                    this.loadDBLibraries()
                    return SQLiteDataProvider(this.settings.sqliteSettings)
                }
                DataProvider.Type.MYSQL -> {
                    this.loadMySQLLibrary()
                    this.loadDBLibraries()
                    return MySQLDataProvider(this.settings.mySQLSettings)
                }
                DataProvider.Type.POSTGRESQL -> {
                    this.loadPostgreSQLLibraries()
                    this.loadDBLibraries()
                    return PostgreSQLDataProvider(this.settings.postgreSQLSettings)
                }
                else -> throw RuntimeException("Unknown provider")
            }
        } catch (e: Exception) {
            throw DataProviderException("Cannot instantiate provider " + type.name, e, type)
        }
    }

    fun save(initiator: String = "auto save") {
        this.logger.info(this.messenger.getMessage("save-started", "@initiator", initiator))
        val saved = this.vaultManager.save().toString()
        this.logger.info(this.messenger.getMessage("save-finished", "@amount", saved))
    }

    private fun initVaults() {
        this.vaultManager = VaultManager
    }

    private fun initAutoSave() {
        if (this.settings.autoSave) {
            this.server.scheduler.scheduleDelayedRepeatingTask(this, { this.save() }, this.settings.autoSavePeriod * 20, this.settings.autoSavePeriod * 20)
        }
    }

    private fun initCommands() {
        this.mainCommand = VaultCommand
        this.server.commandMap.register(this.mainCommand.name, this.mainCommand)

        this.registerCommand(CreateVaultCommand)
        this.registerCommand(RemoveVaultCommand)
        this.registerCommand(OpenVaultCommand)
        this.registerCommand(VaultListCommand)
        this.registerCommand(SaveCommand)
        this.registerCommand(MigrateCommand)
    }

    private fun initDataProvider(): Boolean {
        try {
            this.provider = this.getProviderInstance(this.settings.provider)
            this.logger.info(TextFormat.GREEN.toString() + this.messenger.getMessage("loading.data-provider", "@name", this.settings.provider.name))
        } catch (e: Exception) {
            this.logger.alert(TextFormat.RED.toString() + this.messenger.getMessage("loading.error.data-provider-error", arrayOf("@err", "@provider"), arrayOf(e.message as String, this.settings.provider.name)))
            this.forceShutdown = true
            this.logger.alert(Utils.getExceptionMessage(e))
            this.pluginLoader.disablePlugin(this)
            return false
        }
        return true
    }

    private fun loadLibraries(): Boolean {
        try {
            LibraryLoader.load("org.datanucleus:javax.jdo:3.2.0-m11")
            LibraryLoader.load("org.datanucleus:datanucleus-core:5.2.0-release")
        } catch (e: LibraryLoadException) {
            this.logger.alert(TextFormat.RED.toString() + this.messenger.getMessage("loading.error.fastjson"))
            this.logger.alert(Utils.getExceptionMessage(e))
            this.forceShutdown = true
            this.pluginLoader.disablePlugin(this)
            return false
        }
        return true
    }

    private fun loadDBLibraries() {
        LibraryLoader.load("org.datanucleus:datanucleus-api-jdo:5.2.0-release")
        LibraryLoader.load("org.datanucleus:datanucleus-rdbms:5.2.0-release")
    }

    private fun loadMySQLLibrary() {
        LibraryLoader.load("mysql:mysql-connector-java:8.0.15")
    }

    private fun loadSQLiteLibraries() {
        LibraryLoader.load("org.xerial:sqlite-jdbc:3.27.2.1")
    }

    private fun loadPostgreSQLLibraries() {
        LibraryLoader.load("postgresql:postgresql:9.1-901-1.jdbc4")
    }

    private fun initEventHandler() = this.server.pluginManager.registerEvents(InventoryEventHandler, this)

    private fun initSettings(): Boolean {
        try {
            this.settings = Settings
        } catch (e: Exception) {
            this.logger.info(TextFormat.RED.toString() + this.messenger.getMessage("loading.error.resource", "@err", e.message as String))
            this.forceShutdown = true
            this.logger.alert(Utils.getExceptionMessage(e))
            this.pluginLoader.disablePlugin(this)
            return false
        }
        return true
    }

    private fun createDirectories(): Boolean {
        return this.createFolder(MAIN_FOLDER) && this.createFolder(LANG_FOLDER) && this.createFolder(VAULTS_FOLDER) && this.createFolder(DB_FOLDER)
    }

    private fun createFolder(path: String): Boolean {
        val folder = File(path)
        if (!folder.exists() && !folder.mkdirs()) {
            this.forceShutdown = true
            this.logger.warning(this.messenger.getMessage("loading.error.folder", "@path", path))
            this.pluginLoader.disablePlugin(this)
            return false
        }
        return true
    }

    private fun initMessenger(): Boolean {
        try {
            this.messenger = Messenger
        } catch (e: Exception) {
            this.logger.alert(TextFormat.RED.toString() + "Messenger initializing error")

            this.logger.alert(TextFormat.RED.toString() + Utils.getExceptionMessage(e))

            this.logger.alert(TextFormat.RED.toString() + "Disabling plugin...")
            this.forceShutdown = true
            this.pluginLoader.disablePlugin(this)
            return false
        }
        return true
    }

    private fun checkUpdate() {
        try {
            val response = httpGetRequestJson(VERSION_URL)
            val ver = response["tag_name"] as String
            val description = response["name"] as String
            if (ver.isEmpty()) return
            if (compareVersions(this.description.version, ver) == ver) {
                this.logger.info(this.messenger.getMessage("loading.init.update-available", "@ver", ver))
                this.logger.info(this.messenger.getMessage("loading.init.update-description", "@description", description))

                if (this.settings.updateNotifier) {
                    this.newVersion = Pair(ver, description)
                    this.server.pluginManager.registerEvents(NotifierEventHandler, this)
                }
            }
        } catch (ignore: Exception) {
        }
    }

    private fun loadPlaceholders() {
        if (this.server.pluginManager.getPlugin("PlaceholderAPI") != null) {
            PlaceholdersLoader
        }
    }

    override fun onDisable() {
        this.logger.info(TextFormat.GREEN.toString() + this.messenger.getMessage("disabling.start"))
        this.vaultManager.save()
        this.logger.info(TextFormat.GREEN.toString() + this.messenger.getMessage("disabling.success"))
    }
}
