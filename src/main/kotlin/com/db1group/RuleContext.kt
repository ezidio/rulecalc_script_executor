package com.db1group

/**
 * Classe que disponibiliza ao script acesso a funções externas
 */
class RuleContext(val contract: ContractVersion, val executor: ScriptExecutor) {

    fun log(message:String) {
        println("[${contract.customer}] $message")
    }

    fun load(type: String): List<BilledData> {
        return this.executor.repository.listByType(contract, type)
    }

    fun param(name: String): Double {
        return this.contract.parameters[name]?.toDouble() ?: throw Exception("Parametro não existe: $name")
    }

    fun persist(value: Double) {
        println("Gravando valor $value")
    }
}