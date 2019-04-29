package Sergey_Dertan.SVault.provider.database

import Sergey_Dertan.SVault.provider.DataProvider
import Sergey_Dertan.SVault.settings.SQLiteSettings
import org.datanucleus.metadata.PersistenceUnitMetaData
import org.sqlite.JDBC

class SQLiteDataProvider(settings: SQLiteSettings) : DatabaseDataProvider() {

    init {
        val pumd = PersistenceUnitMetaData("dynamic-unit", "RESOURCE_LOCAL", null)
        pumd.addProperty("javax.jdo.option.ConnectionDriverName", JDBC::class.java.name)
        pumd.addProperty("javax.jdo.option.ConnectionURL", "jdbc:sqlite:" + settings.databaseFile)
        pumd.addProperty("javax.jdo.option.ConnectionUserName", "")
        pumd.addProperty("javax.jdo.option.ConnectionPassword", "")
        this.init(pumd)
    }

    override fun getType(): DataProvider.Type = DataProvider.Type.SQLITE
}
