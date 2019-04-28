package Sergey_Dertan.SVault.command

import Sergey_Dertan.SVault.main.SVaultMain
import Sergey_Dertan.SVault.messenger.Messenger
import cn.nukkit.command.Command
import cn.nukkit.command.CommandSender

class SaveCommand(private val main: SVaultMain) : Command("save") {

    private val messenger = Messenger.getInstance()

    init {
        this.permission = "svault.command.save"
        this.description = this.messenger.getMessage("command.save.description")
        this.commandParameters = mutableMapOf()
    }

    override fun execute(sender: CommandSender, s: String, args: Array<out String>): Boolean {
        if (!this.testPermissionSilent(sender)) {
            this.messenger.sendMessage(sender, "command.save.permission")
            return false
        }
        this.messenger.sendMessage(sender, "command.save.saving")
        this.main.save(sender.name)
        return true
    }
}