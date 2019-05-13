package Sergey_Dertan.SVault.command

import Sergey_Dertan.SVault.messenger.Messenger
import cn.nukkit.command.Command
import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandParameter
import java.util.*

object VaultCommand : Command("vault") {

    private val messenger = Messenger
    private val commands = mutableMapOf<String, Command>()

    init {
        this.description = this.messenger.getMessage("command.$name.description")
        this.permission = "svault.command.$name"
    }

    override fun execute(sender: CommandSender, s: String, args: Array<out String>): Boolean {
        if (!this.testPermissionSilent(sender)) {
            this.messenger.sendMessage(sender, "command.$name.permission")
            return false
        }
        if (args.isEmpty() || args[0].equals("help", true)) {
            this.messenger.sendMessage(sender, "command.$name.available-commands")
            this.commands.values.filter { sender.hasPermission(it.description) }.forEach { sender.sendMessage("${it.name} - {${it.description}}") }
            return false
        }
        val cmd = this.commands[args[0]]
        if (cmd == null) {
            this.messenger.sendMessage(sender, "command.$name.command-not-found", "@name", args[0])
            return false
        }
        val newArgs = if (args.size == 1) arrayOf() else Arrays.copyOfRange(args, 1, args.size)
        cmd.execute(sender, cmd.name, newArgs)
        return false
    }

    fun registerCommand(command: Command) {
        this.commands[command.name] = command
        this.updateArguments()
    }

    private fun updateArguments() {
        val params = mutableMapOf<String, Array<CommandParameter>>()
        for (command in this.commands.values) {
            val p = mutableListOf<CommandParameter>()
            p.add(CommandParameter(command.name, false, arrayOf(command.name)))
            command.commandParameters.values.forEach { p.addAll(it.toList()) }
            params[command.name] = p.toTypedArray()
        }
        this.commandParameters = params
    }
}
