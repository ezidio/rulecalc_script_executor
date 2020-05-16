package com.db1group.contract

import java.util.*

class BilledData(
    val type:String,
    val customer:UUID,
    val instance:Int,
    val metadata:Map<String, Any>
)