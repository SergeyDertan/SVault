package Sergey_Dertan.SVault.utils

import Sergey_Dertan.SVault.provider.DataProvider

class DataProviderException(msg: String, e: Exception, val provider: DataProvider.Type) : RuntimeException(msg, e)
