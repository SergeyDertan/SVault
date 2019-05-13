package Sergey_Dertan.SVault.command

import cn.nukkit.Player
import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandParamType
import cn.nukkit.command.data.CommandParameter

object CreateVaultCommand : SVaultCommand("create") {

    init {
        this.commandParameters = mapOf(
                Pair("vault-name", arrayOf(CommandParameter("vault-name", CommandParamType.STRING, false)))
        )
    }

    override fun execute(sender: CommandSender, s: String, args: Array<out String>): Boolean {
        if (!this.testPermissionSilent(sender)) {
            this.messenger.sendMessage(sender, "command.$name.permission")
            return false
        }
        if (sender !is Player) {
            this.messenger.sendMessage(sender, "command.$name.in-game")
            return false
        }
        if (!this.vaultManager.hasAmountPermission(sender, this.vaultManager.getVaultsAmount(sender) + 1)) {
            this.messenger.sendMessage(sender, "command.$name.max")
            return false
        }
        if (args.isEmpty()) {
            this.messenger.sendMessage(sender, "command.$name.usage")
            return false
        }
        if (!args[0].matches(Regex("[a-zA-Z0-9]*"))) {
            this.messenger.sendMessage(sender, "command.$name.wrong-name")
            return false
        }
        if (this.vaultManager.vaultExists(sender.name, args[0])) {
            this.messenger.sendMessage(sender, "command.$name.exists")
            return false
        }
        this.vaultManager.createVault(sender, args[0])
        this.messenger.sendMessage(sender, "command.$name.success")
        return true
    }
}
