package Sergey_Dertan.SVault.provider.database

import Sergey_Dertan.SVault.provider.DataProvider
import Sergey_Dertan.SVault.settings.PostgreSQLSettings
import com.mysql.cj.jdbc.Driver
import org.datanucleus.metadata.PersistenceUnitMetaData

class PostgreSQLDataProvider(settings: PostgreSQLSettings) : DatabaseDataProvider() {

    init {
        val pumd = PersistenceUnitMetaData("dynamic-unit", "RESOURCE_LOCAL", null)
        pumd.addProperty("javax.jdo.option.ConnectionDriverName", Driver::class.java.name)
        pumd.addProperty(
                "javax.jdo.option.ConnectionURL",
                "jdbc:mysql://" + settings.address + ":" + settings.port + "/" + settings.database + "?useSSL=false&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC"
        )
        pumd.addProperty("javax.jdo.option.ConnectionUserName", settings.username)
        pumd.addProperty("javax.jdo.option.ConnectionPassword", settings.password)
        this.init(pumd)
    }

    override fun getType(): DataProvider.Type = DataProvider.Type.POSTGRESQL
}
