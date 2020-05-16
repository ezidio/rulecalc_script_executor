package com.db1group.contract

import java.math.BigDecimal
import java.util.*

class ContractVersion(
    val customer: UUID,
    val parameters:Map<String, BigDecimal>
)