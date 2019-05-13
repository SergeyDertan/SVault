package Sergey_Dertan.SVault.command

import Sergey_Dertan.SVault.main.SVaultMain
import Sergey_Dertan.SVault.messenger.Messenger
import cn.nukkit.command.Command
import cn.nukkit.command.CommandSender

object SaveCommand : Command("save") {

    private val messenger = Messenger
    private val main = SVaultMain.instance

    init {
        this.permission = "svault.command.$name"
        this.description = this.messenger.getMessage("command.$name.description")
        this.commandParameters = mutableMapOf()
    }

    override fun execute(sender: CommandSender, s: String, args: Array<out String>): Boolean {
        if (!this.testPermissionSilent(sender)) {
            this.messenger.sendMessage(sender, "command.$name.permission")
            return false
        }
        this.messenger.sendMessage(sender, "command.$name.saving")
        this.main.save(sender.name)
        return true
    }
}
