package Sergey_Dertan.SVault.messenger

import Sergey_Dertan.SVault.main.SVaultMain
import Sergey_Dertan.SVault.main.SVaultMain.Companion.LANG_FOLDER
import Sergey_Dertan.SVault.main.SVaultMain.Companion.MAIN_FOLDER
import Sergey_Dertan.SVault.utils.Utils.copyResource
import Sergey_Dertan.SVault.utils.Utils.resourceExists
import cn.nukkit.Server
import cn.nukkit.command.CommandSender
import cn.nukkit.utils.Config
import cn.nukkit.utils.Utils
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.File

object Messenger {

    const val DEFAULT_LANGUAGE = "eng"

    @Suppress("WEAKER_ACCESS")
    val language: String
    private val messages: Map<String, String>

    init {
        var lang: String? = null
        if (File(MAIN_FOLDER + "config.yml").exists()) {
            val cnf = Config(MAIN_FOLDER + "config.yml", Config.YAML).all
            if (cnf.containsKey("language") && !(cnf["language"] as String).equals("default", true)) {
                lang = cnf["language"] as String
            }
        }
        if (lang == null) {
            lang = Server.getInstance().language.lang
        }
        if (!resourceExists("$lang.yml", "resources/lang", SVaultMain::class.java)) lang = DEFAULT_LANGUAGE
        this.language = lang as String
        copyResource("$lang.yml", "resources/lang", LANG_FOLDER, SVaultMain::class.java)
        val dumperOptions = DumperOptions()
        dumperOptions.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
        val yaml = Yaml(dumperOptions)
        @Suppress("UNCHECKED_CAST")
        this.messages = yaml.loadAs<HashMap<*, *>>(Utils.readFile(File("$LANG_FOLDER$lang.yml")), HashMap::class.java) as Map<String, String>
    }

    fun getMessage(message: String, search: Array<String> = emptyArray(), replace: Array<String> = emptyArray()): String {
        var msg = this.messages.getOrDefault(message, message)
        if (search.size == replace.size) {
            for (i in search.indices) {
                var var1 = search[i]
                if (var1[0] != '{') var1 = "{$var1"
                if (var1[var1.length - 1] != '}') var1 = "$var1}"
                msg = msg.replace(var1, replace[i])
            }
        }
        return msg
    }

    fun getMessage(message: String, search: String, replace: String): String {
        return this.getMessage(message, arrayOf(search), arrayOf(replace))
    }

    fun sendMessage(target: CommandSender, message: String, search: Array<String> = emptyArray(), replace: Array<String> = emptyArray()) {
        target.sendMessage(this.getMessage(message, search, replace))
    }

    fun sendMessage(target: CommandSender, message: String, search: String, replace: String) {
        this.sendMessage(target, message, arrayOf(search), arrayOf(replace))
    }
}