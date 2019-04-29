package Sergey_Dertan.SVault.command

import Sergey_Dertan.SVault.vault.VaultManager
import cn.nukkit.Player
import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandParamType
import cn.nukkit.command.data.CommandParameter

class RemoveVaultCommand(vaultManager: VaultManager) : SVaultCommand("remove", vaultManager) {

    init {
        this.commandParameters = mapOf(
            Pair("remove-vault-name", arrayOf(CommandParameter("vault-name", CommandParamType.STRING, false))),
            Pair("remove-owner-name", arrayOf(CommandParameter("owner", CommandParamType.STRING, true)))
        )
    }

    override fun execute(sender: CommandSender, s: String, args: Array<out String>): Boolean {
        if (!this.testPermissionSilent(sender)) {
            this.messenger.sendMessage(sender, "command.remove.permission")
            return false
        }
        if (args.isEmpty()) {
            this.messenger.sendMessage(sender, "command.remove.usage")
            return false
        }
        if (args.size == 1) {
            if (sender !is Player) {
                this.messenger.sendMessage(sender, "command.remove.in-game")
                return false
            }
            if (!this.vaultManager.vaultExists(sender.name, args[0])) {
                this.messenger.sendMessage(sender, "command.remove.not-found")
                return false
            }
            this.vaultManager.removeVault(sender.name, args[0])
            this.messenger.sendMessage(sender, "command.remove.success")
        } else {
            if (!this.vaultManager.vaultExists(args[1], args[0])) {
                this.messenger.sendMessage(sender, "command.remove.not-found")
                return false
            }
            if (!sender.hasPermission("svault.admin")) {
                this.messenger.sendMessage(sender, "command.remove.permission")
                return false
            }
            this.vaultManager.removeVault(args[1], args[0])
            this.messenger.sendMessage(sender, "command.remove.success")
        }
        return true
    }
}
