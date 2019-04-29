package Sergey_Dertan.SVault.settings

import Sergey_Dertan.SVault.main.SVaultMain

class SQLiteSettings internal constructor(databaseFile: String) {

    val databaseFile = databaseFile.replace("{@plugin-folder}", SVaultMain.MAIN_FOLDER)
}
