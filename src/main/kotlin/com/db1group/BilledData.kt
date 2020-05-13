package com.db1group

import java.util.*

class BilledData(
    val type:String,
    val customer:UUID,
    val instance:Int,
    val metadata:Map<String, Any>
)