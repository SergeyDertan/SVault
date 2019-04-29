package Sergey_Dertan.SVault.command

import Sergey_Dertan.SVault.messenger.Messenger
import Sergey_Dertan.SVault.vault.VaultManager
import cn.nukkit.command.Command

abstract class SVaultCommand(name: String, protected val vaultManager: VaultManager) : Command(name) {

    protected val messenger = Messenger.getInstance()

    init {
        this.permission = "svault.command.$name"
        this.description = this.messenger.getMessage("command.$name.description")
    }
}
