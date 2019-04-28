package Sergey_Dertan.SVault.utils

import cn.nukkit.utils.Config
import cn.nukkit.utils.ConfigSection
import com.google.gson.Gson
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object Utils {

    @Throws(IOException::class)
    fun httpGetRequestJson(url: String): Map<String, Any> {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        val reader = BufferedReader(InputStreamReader(connection.inputStream))
        val gson = Gson()
        var map: Map<String, Any> = HashMap()
        map = gson.fromJson(reader.readLine(), map.javaClass)
        return map
    }

    /*---------------- resources ------------------*/
    @Throws(IOException::class)
    fun copyResource(fileName: String, sourceFolder: String, targetFolder: String, clazz: Class<*>, fixMissingContents: Boolean) {
        var sourceFolder = sourceFolder
        var targetFolder = targetFolder
        //TODO remove useless
        if (sourceFolder[sourceFolder.length - 1] != '/') sourceFolder += '/'.toString()
        if (targetFolder[targetFolder.length - 1] != '/') targetFolder += '/'.toString()
        val file = File(targetFolder + fileName)
        if (!file.exists()) {
            cn.nukkit.utils.Utils.writeFile(file, clazz.classLoader.getResourceAsStream(sourceFolder + fileName))
            return
        }
        if (!fixMissingContents) return
        val var3 = Config(file.absolutePath, Config.YAML)
        val dumperOptions = DumperOptions()
        dumperOptions.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
        val yaml = Yaml(dumperOptions)
        @Suppress("UNCHECKED_CAST")
        val var1 = yaml.loadAs(clazz.classLoader.getResourceAsStream(sourceFolder + fileName), HashMap::class.java) as Map<String, Any>

        val var4 = ConfigSection(LinkedHashMap(var3.all))
        val changed = copyMapOfMaps(var1, var4)
        if (changed) {
            val var5 = LinkedHashMap<String, Any>()
            var4.entries.forEach { var5[it.key] = it.value }
            var3.setAll(var5)
            var3.save()
        }
    }

    /**
     * recursive copy map of maps
     */
    fun copyMapOfMaps(from: Map<String, Any>, to: MutableMap<String, Any>): Boolean {
        var changed = false
        if (from.size > to.size) changed = true
        for ((key, value) in from) {
            if (!to.containsKey(key)) {
                changed = true
                to[key] = value
            }
        }
        val var1 = from.entries.iterator()
        while (var1.hasNext()) {
            val next = var1.next()
            if (next.value is Map<*, *>) {
                @Suppress("UNCHECKED_CAST")
                val c = copyMapOfMaps(next.value as Map<String, Any>, to[next.key] as MutableMap<String, Any>)
                if (!changed) changed = c
            }
        }
        return changed
    }

    @Throws(IOException::class)
    fun copyResource(fileName: String, sourceFolder: String, targetFolder: String, clazz: Class<*>) {
        copyResource(fileName, sourceFolder, targetFolder, clazz, true)
    }

    fun resourceExists(fileName: String, folder: String, clazz: Class<*>): Boolean {
        var folder = folder
        if (!folder.endsWith("/")) folder += '/'.toString()
        return clazz.classLoader.getResource(folder + fileName) != null
    }

    /*---------------- resources end --------------*/

    //slices array into pieces with the same size
    fun <T> sliceArray(array: Array<T>, pieces: Int, keepEmpty: Boolean): List<List<T>> {
        val result = mutableListOf<MutableList<T>>()
        for (i in 0 until pieces) {
            result.add(ArrayList())
        }

        var i = 0

        for (obj in array) {
            if (i == pieces) i = 0
            result[i].add(obj)
            ++i
        }
        if (!keepEmpty) {
            result.removeIf { it.isEmpty() }
        }
        return result
    }

    /**
     * @return greater version string or empty string if they are equal
     */
    fun compareVersions(first: String, second: String): String {
        if (first == second) return ""
        val f = first.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val s = second.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        val bigger = if (f.size >= s.size) f else s
        val smaller = if (f.size < s.size) f else s

        for (i in smaller.indices) {
            if (Integer.parseInt(smaller[i]) > Integer.parseInt(bigger[i])) {
                return smaller.joinToString(".")
            } else if (Integer.parseInt(smaller[i]) < Integer.parseInt(bigger[i])) {
                return bigger.joinToString(".")
            }
            if (smaller.size == i + 1 && smaller.size < bigger.size) return bigger.joinToString(".")
        }
        throw RuntimeException("Unreachable code reached")
    }
}
