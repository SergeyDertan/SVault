package Sergey_Dertan.SVault.command

import Sergey_Dertan.SVault.vault.VaultManager
import cn.nukkit.Player
import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandParamType
import cn.nukkit.command.data.CommandParameter

class OpenVaultCommand(vaultManager: VaultManager) : SVaultCommand("open", vaultManager) {

    init {
        this.commandParameters = mapOf(
            Pair("open-vault-name", arrayOf(CommandParameter("vault-name", CommandParamType.STRING, false))),
            Pair("open-vault-owner", arrayOf(CommandParameter("vault-owner", CommandParamType.STRING, true)))
        )
    }

    override fun execute(sender: CommandSender, s: String?, args: Array<out String>): Boolean {
        if (!this.testPermissionSilent(sender)) {
            this.messenger.sendMessage(sender, "command.open.permission")
            return false
        }
        if (args.isEmpty()) {
            this.messenger.sendMessage(sender, "command.open.usage")
            return false
        }
        if (sender !is Player) {
            this.messenger.sendMessage(sender, "command.remove.in-game")
            return false
        }
        if (args.size == 1) {
            if (!this.vaultManager.vaultExists(sender.name, args[0])) {
                this.messenger.sendMessage(sender, "command.open.not-found")
                return false
            }
            this.vaultManager.open(sender, args[0])
        } else {
            if (!this.vaultManager.vaultExists(args[1], args[0])) {
                this.messenger.sendMessage(sender, "command.open.not-found")
                return false
            }
            if (!sender.hasPermission("svault.admin")) {
                this.messenger.sendMessage(sender, "command.open.permission")
                return false
            }
            this.vaultManager.open(sender, args[0], args[1])
        }
        return true
    }
}