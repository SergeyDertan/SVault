package Sergey_Dertan.SVault.provider

import Sergey_Dertan.SVault.main.SVaultMain.Companion.VAULTS_FOLDER
import Sergey_Dertan.SVault.provider.dataobject.VaultDataObject
import cn.nukkit.utils.Config
import java.io.File

class YAMLDataProvider : DataProvider {

    override fun loadVault(player: String, name: String): VaultDataObject? {
        val file = File("$VAULTS_FOLDER$player$name.yml")
        if (!file.exists()) return VaultDataObject("", "", "")
        val f = Config(file, Config.YAML)
        return VaultDataObject(player, f["name"] as String, f["items"] as String)
    }

    override fun loadVaultList(): List<VaultDataObject> {
        val list = mutableListOf<VaultDataObject>()
        for (file in File(VAULTS_FOLDER).listFiles()) {
            if (file.isDirectory || !file.name.endsWith(".yml")) continue
            val f = Config(file, Config.YAML)
            try {
                list.add(VaultDataObject(f["player"] as String, f["name"] as String, f["items"] as String))
            } catch (ignore: Exception) {
            }
        }
        return list
    }

    override fun saveVault(vdo: VaultDataObject) {
        val f = Config("$VAULTS_FOLDER${vdo.player}${vdo.name}.yml", Config.YAML)
        f["player"] = vdo.player
        f["items"] = vdo.items
        f["name"] = vdo.name
        f.save()
    }

    override fun removeVault(player: String, name: String) {
        File("$VAULTS_FOLDER$player$name.yml").delete()
    }

    override fun getType(): DataProvider.Type = DataProvider.Type.YAML
}
