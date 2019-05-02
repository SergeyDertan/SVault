package Sergey_Dertan.SVault.command

import Sergey_Dertan.SVault.vault.VaultManager
import cn.nukkit.Player
import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandParamType
import cn.nukkit.command.data.CommandParameter

class CreateVaultCommand(vaultManager: VaultManager) : SVaultCommand("create", vaultManager) {

    init {
        this.commandParameters = mapOf(
            Pair("vault-name", arrayOf(CommandParameter("vault-name", CommandParamType.STRING, false)))
        )
    }

    override fun execute(sender: CommandSender, s: String, args: Array<out String>): Boolean {
        if (!this.testPermissionSilent(sender)) {
            this.messenger.sendMessage(sender, "command.create.permission")
            return false
        }
        if (sender !is Player) {
            this.messenger.sendMessage(sender, "command.create.in-game")
            return false
        }
        if (!this.vaultManager.hasAmountPermission(sender, this.vaultManager.getVaultsAmount(sender) + 1)) {
            this.messenger.sendMessage(sender, "command.create.max")
            return false
        }
        if (args.isEmpty()) {
            this.messenger.sendMessage(sender, "command.create.usage")
            return false
        }
        if (!args[0].matches(Regex("[a-zA-Z0-9]*"))) {
            this.messenger.sendMessage(sender, "command.create.wrong-name")
            return false
        }
        if (this.vaultManager.vaultExists(sender.name, args[0])) {
            this.messenger.sendMessage(sender, "command.create.exists")
            return false
        }
        this.vaultManager.createVault(sender, args[0])
        this.messenger.sendMessage(sender, "command.create.success")
        return true
    }
}
