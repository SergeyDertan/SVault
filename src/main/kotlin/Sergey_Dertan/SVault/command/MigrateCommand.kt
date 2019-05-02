package Sergey_Dertan.SVault.command

import Sergey_Dertan.SVault.main.SVaultMain
import Sergey_Dertan.SVault.provider.DataProvider
import Sergey_Dertan.SVault.utils.DataProviderException
import cn.nukkit.command.CommandSender
import cn.nukkit.command.data.CommandParameter

class MigrateCommand(private val main: SVaultMain) : SVaultCommand("migrate", main.vaultManager) {

    init {
        val providers = mutableListOf<String>()
        DataProvider.Type.values().forEach { providers.add(it.name.toLowerCase()) }
        providers.remove(DataProvider.Type.UNKNOWN.name.toLowerCase())

        this.commandParameters = mapOf(
            Pair("from-provider", arrayOf(CommandParameter("from", false, providers.toTypedArray()))),
            Pair("to-provider", arrayOf(CommandParameter("to", false, providers.toTypedArray())))
        )
    }

    override fun execute(sender: CommandSender, s: String, args: Array<out String>): Boolean {
        if (!this.testPermissionSilent(sender)) {
            this.messenger.sendMessage(sender, "command.migrate.permission")
            return false
        }
        if (args.size < 2) {
            this.messenger.sendMessage(sender, "command.migrate.usage")
            return false
        }
        val fr = DataProvider.Type.fromString(args[0]) //from
        val tt = DataProvider.Type.fromString(args[1]) //to
        if (fr == DataProvider.Type.UNKNOWN || tt == DataProvider.Type.UNKNOWN) {
            this.messenger.sendMessage(sender, "command.migrate.unknown-provider")
            return false
        }
        if (fr == tt) {
            this.messenger.sendMessage(sender, "command.migrate.same-provider")
            return false
        }
        val from: DataProvider
        val to: DataProvider
        try {
            from = this.main.getProviderInstance(fr)
            to = this.main.getProviderInstance(tt)
        } catch (e: DataProviderException) {
            this.messenger.sendMessage(sender, "command.migrate.error", "@provider", e.provider.name)
            return false
        }
        val list = from.loadVaultList()
        to.saveVaultList(list)
        this.messenger.sendMessage(sender, "command.migrate.success", "@amount", list.size.toString())
        return true
    }
}
