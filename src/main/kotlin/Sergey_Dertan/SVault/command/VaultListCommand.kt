package Sergey_Dertan.SVault.command

import cn.nukkit.Player
import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandParamType
import cn.nukkit.command.data.CommandParameter

object VaultListCommand : SVaultCommand("list") {

    init {
        this.commandParameters = mapOf(
                Pair("list-vaults-owner", arrayOf(CommandParameter("vaults-owner", CommandParamType.STRING, true)))
        )
    }

    override fun execute(sender: CommandSender, s: String, args: Array<out String>): Boolean {
        if (!this.testPermissionSilent(sender)) {
            this.messenger.sendMessage(sender, "command.$name.permission")
            return false
        }
        if (args.isEmpty()) {
            if (sender !is Player) {
                this.messenger.sendMessage(sender, "command.$name.in-game")
                return false
            }
            this.messenger.sendMessage(sender, "command.$name.list.own", "@list", this.vaultManager.getVaultList(sender.name).joinToString { ", " })
        } else {
            if (!sender.hasPermission("svault.admin")) {
                this.messenger.sendMessage(sender, "command.$name.permission")
                return false
            }
            this.messenger.sendMessage(sender, "command.$name.list.other", arrayOf("@player", "@list"), arrayOf(args[0], this.vaultManager.getVaultList(args[0]).joinToString()))
        }
        return true
    }
}
