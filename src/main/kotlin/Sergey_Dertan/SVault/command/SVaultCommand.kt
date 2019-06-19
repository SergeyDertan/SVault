package Sergey_Dertan.SVault.command

import Sergey_Dertan.SVault.main.SVaultMain
import Sergey_Dertan.SVault.messenger.Messenger
import Sergey_Dertan.SVault.vault.VaultManager
import cn.nukkit.command.Command
import cn.nukkit.command.PluginIdentifiableCommand

abstract class SVaultCommand(name: String) : Command(name), PluginIdentifiableCommand {

    protected val messenger = Messenger
    protected val vaultManager = VaultManager

    init {
        this.permission = "svault.command.$name"
        this.description = this.messenger.getMessage("command.$name.description")
    }

    override fun getPlugin() = SVaultMain.instance
}
