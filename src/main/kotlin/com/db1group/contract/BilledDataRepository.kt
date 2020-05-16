package com.db1group.contract

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

class BilledDataRepository {
    val mapper = ObjectMapper().registerKotlinModule()

    fun listByType(contract: ContractVersion, type: String): List<BilledData> {
        val content = Thread.currentThread().contextClassLoader.getResource("data.json")
        val data = mapper.readValue(content, object:TypeReference<List<BilledData>>() { })
        return data.filter { it.type == type && it.customer == contract.customer }
    }
}