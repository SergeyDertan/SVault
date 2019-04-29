package Sergey_Dertan.SVault.provider.database

import Sergey_Dertan.SVault.main.SVaultMain
import Sergey_Dertan.SVault.provider.DataProvider
import Sergey_Dertan.SVault.provider.dataobject.VaultDataObject
import org.datanucleus.api.jdo.JDOPersistenceManagerFactory
import org.datanucleus.exceptions.NucleusException
import org.datanucleus.metadata.PersistenceUnitMetaData
import javax.jdo.JDOException
import javax.jdo.PersistenceManager
import javax.jdo.PersistenceManagerFactory

abstract class DatabaseDataProvider : DataProvider {

    protected lateinit var factory: PersistenceManagerFactory
    protected lateinit var pm: PersistenceManager

    protected fun init(pumd: PersistenceUnitMetaData) {
        pumd.addClassName(VaultDataObject::class.java.name)
        pumd.excludeUnlistedClasses = true
        pumd.addProperty("datanucleus.schema.autoCreateTables", "true")
        this.factory = object : JDOPersistenceManagerFactory(pumd, null) {
            override fun initialiseMetaData(pumd: PersistenceUnitMetaData?) {
                this.nucleusContext.metaDataManager.setAllowXML(this.configuration.getBooleanProperty("datanucleus.metadata.allowXML"))
                this.nucleusContext.metaDataManager.setAllowAnnotations(this.configuration.getBooleanProperty("datanucleus.metadata.allowAnnotations"))
                this.nucleusContext.metaDataManager.setValidate(this.configuration.getBooleanProperty("datanucleus.metadata.xml.validate"))
                this.nucleusContext.metaDataManager.isDefaultNullable = this.configuration.getBooleanProperty("datanucleus.metadata.defaultNullable")
                if (pumd != null) {
                    try {
                        this.nucleusContext.metaDataManager.loadPersistenceUnit(pumd, SVaultMain::class.java.classLoader)
                        if (pumd.validationMode != null) {
                            this.configuration.setProperty("datanucleus.validation.mode", pumd.validationMode)
                        }
                    } catch (var3: NucleusException) {
                        throw JDOException(var3.message, var3)
                    }
                }
                val allowMetadataLoad = this.nucleusContext.configuration.getBooleanProperty("datanucleus.metadata.allowLoadAtRuntime")
                if (!allowMetadataLoad) {
                    this.nucleusContext.metaDataManager.setAllowMetaDataLoad(false)
                }
            }
        }
        this.pm = this.factory.getPersistenceManager()
        this.pm.detachAllOnCommit = true
    }

    override fun loadVaultList(): Collection<VaultDataObject> {
        val query = this.pm.newQuery(VaultDataObject::class.java)
        @Suppress("UNCHECKED_CAST")
        return query.execute() as Collection<VaultDataObject>
    }

    override fun loadVault(player: String, name: String): VaultDataObject? {
        val query = this.pm.newQuery(VaultDataObject::class.java, "name == '$name' && player == '$player'")
        val result = query.execute() as Collection<*>
        return if (result.isEmpty()) null else result.iterator().next() as VaultDataObject
    }

    override fun saveVault(vdo: VaultDataObject) {
        val tr = this.pm.currentTransaction()
        tr.begin()

        val existing = this.loadVault(vdo.player, vdo.name)
        if (existing == null) {
            this.pm.makePersistent(vdo)
        } else {
            if (existing.items != vdo.items) existing.items = vdo.items
        }
        tr.commit()
    }

    override fun removeVault(player: String, name: String) {
        val vdo = this.loadVault(player, name)
        vdo?.let { this.pm.deletePersistent(it) }
    }

    override fun close() {
        this.pm.close()
        this.factory.close()
    }
}
