package Sergey_Dertan.SVault.settings

class PostgreSQLSettings internal constructor(data: Map<String, Any>) {

    val address = data["address"] as String
    val port = (data["port"] as Number).toInt()
    val username = data["username"] as String
    val password = data["password"] as String
    val database = data["database"] as String
}
