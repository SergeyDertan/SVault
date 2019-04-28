package Sergey_Dertan.SVault.main

import Sergey_Dertan.SVault.command.*
import Sergey_Dertan.SVault.event.InventoryEventHandler
import Sergey_Dertan.SVault.event.NotifierEventHandler
import Sergey_Dertan.SVault.messenger.Messenger
import Sergey_Dertan.SVault.provider.DataProvider
import Sergey_Dertan.SVault.provider.YAMLDataProvider
import Sergey_Dertan.SVault.settings.Settings
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

    companion object {
        val MAIN_FOLDER = Server.getInstance().dataPath + "Sergey_Dertan_Plugins/SVault/"
        val LANG_FOLDER = MAIN_FOLDER + "Lang/"
        val VAULTS_FOLDER = MAIN_FOLDER + "Vaults/"
        const val VERSION_URL = "https://api.github.com/repos/SergeyDertan/SVault/releases/latest"

        private lateinit var instance: SVaultMain

        @Suppress("UNUSED")
        fun getInstance(): SVaultMain = instance
    }

    override fun onEnable() {
        val start = System.currentTimeMillis()

        if (!this.createDirectories()) return
        if (!this.initMessenger()) return

        this.logger.info(TextFormat.GREEN.toString() + this.messenger.getMessage("loading.init.start", "@ver", this.description.version))

        this.logger.info(TextFormat.GREEN.toString() + this.messenger.getMessage("loading.init.libraries"))
        if (!this.loadLibraries()) return

        this.logger.info(TextFormat.GREEN.toString() + this.messenger.getMessage("loading.init.settings"))
        if (!this.initSettings()) return

        this.logger.info(TextFormat.GREEN.toString() + this.messenger.getMessage("loading.init.data-provider"))
        if (!this.initDataProvider()) return

        this.logger.info(TextFormat.GREEN.toString() + this.messenger.getMessage("loading.init.vaults"))
        this.initVaults()

        this.logger.info(TextFormat.GREEN.toString() + this.messenger.getMessage("loading.init.events-handlers"))
        this.initEventHandler()

        this.logger.info(TextFormat.GREEN.toString() + this.messenger.getMessage("loading.init.commands"))
        this.initCommands()

        this.initAutoSave()

        this.logger.info(TextFormat.GREEN.toString() + this.messenger.getMessage("loading.init.successful", "@time", (System.currentTimeMillis() - start).toString()))

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
                else -> throw RuntimeException("Unknown provider")
            }
        } catch (e: Exception) {
            throw RuntimeException("Cannot instantiate provider " + type.name, e)
        }
    }

    fun save(initiator: String = "auto save") {
        this.logger.info(this.messenger.getMessage("save-started", "@initiator", initiator))
        val saved = this.vaultManager.save().toString()
        this.logger.info(this.messenger.getMessage("save-finished", "@amount", saved))
    }

    private fun initVaults() {
        this.vaultManager = VaultManager(this.settings, this.provider, this.logger)
    }

    private fun initAutoSave() {
        if (this.settings.autoSave) {
            this.server.scheduler.scheduleDelayedRepeatingTask(this, { this.save() }, this.settings.autoSavePeriod * 20, this.settings.autoSavePeriod * 20)
        }
    }

    private fun initCommands() {
        this.mainCommand = VaultCommand()
        this.server.commandMap.register(this.mainCommand.name, this.mainCommand)

        this.registerCommand(CreateVaultCommand(this.vaultManager))
        this.registerCommand(RemoveVaultCommand(this.vaultManager))
        this.registerCommand(OpenVaultCommand(this.vaultManager))
        this.registerCommand(VaultListCommand(this.vaultManager))
        this.registerCommand(SaveCommand(this))
    }

    private fun initDataProvider(): Boolean {
        try {
            this.provider = this.getProviderInstance(this.settings.provider)
            this.logger.info(TextFormat.GREEN.toString() + this.messenger.getMessage("loading.data-provider", "@name", this.settings.provider.name))
            return true
        } catch (e: Exception) {
            this.logger.alert(TextFormat.RED.toString() + this.messenger.getMessage("loading.error.data-provider-error", arrayOf("@err", "@provider"), arrayOf(e.message as String, this.settings.provider.name)))
            this.forceShutdown = true
            this.logger.alert(Utils.getExceptionMessage(e))
            this.pluginLoader.disablePlugin(this)
            return false
        }

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

    private fun initEventHandler() = this.server.pluginManager.registerEvents(InventoryEventHandler(), this)

    private fun initSettings(): Boolean {
        try {
            this.settings = Settings()
            return true
        } catch (e: Exception) {
            this.logger.info(TextFormat.RED.toString() + this.messenger.getMessage("loading.error.resource", "@err", e.message as String))
            this.forceShutdown = true
            this.logger.alert(Utils.getExceptionMessage(e))
            this.pluginLoader.disablePlugin(this)
            return false
        }

    }

    private fun createDirectories(): Boolean {
        return this.createFolder(MAIN_FOLDER) && this.createFolder(LANG_FOLDER) && this.createFolder(VAULTS_FOLDER)
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
            this.messenger = Messenger()
            return true
        } catch (e: Exception) {
            this.logger.alert(TextFormat.RED.toString() + "Messenger initializing error")

            this.logger.alert(TextFormat.RED.toString() + Utils.getExceptionMessage(e))

            this.logger.alert(TextFormat.RED.toString() + "Disabling plugin...")
            this.forceShutdown = true
            this.pluginLoader.disablePlugin(this)
            return false
        }
    }

    private fun checkUpdate() {
        try {
            val response = httpGetRequestJson(VERSION_URL)
            val ver = response.get("tag_name") as String
            val description = response.get("name") as String
            if (ver.isEmpty()) return
            if (compareVersions(this.description.version, ver).equals(ver)) {
                this.logger.info(this.messenger.getMessage("loading.init.update-available", "@ver", ver))
                this.logger.info(this.messenger.getMessage("loading.init.update-description", "@description", description))

                if (this.settings.updateNotifier) {
                    this.server.pluginManager.registerEvents(NotifierEventHandler(ver, description), this)
                }
            }
        } catch (ignore: Exception) {
        }
    }

    override fun onDisable() {
        this.logger.info(TextFormat.GREEN.toString() + this.messenger.getMessage("disabling.start"))
        this.vaultManager.save()
        this.logger.info(TextFormat.GREEN.toString() + this.messenger.getMessage("disabling.success"))
    }
}