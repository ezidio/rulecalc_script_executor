package com.db1group

import java.math.BigDecimal
import java.util.*

fun main(args: Array<String>) {
    val contrato1 = ContractVersion(
        customer = UUID.fromString("0d048e90-07fb-4678-85b2-dab31fae281c"),
        parameters = mapOf("valorPorLinha" to BigDecimal(2))
    )

    val contrato2 = ContractVersion(
        customer = UUID.fromString("0d048e90-07fb-4678-85b2-dab31fae281c"),
        parameters = mapOf("valorPorLinha" to BigDecimal(2.5))
    )
    val executor = ScriptExecutor(BilledDataRepository())

    val calculoPorLinhaJavaScript = Rule(type = ScriptLanguage.JAVASCRIPT,script = """
        var custoPorLinha = param("valorPorLinha");
        var linhas = load("LINHA");
        persist(linhas.length * custoPorLinha);
    """.trimIndent())
    executor.execute(contrato1, calculoPorLinhaJavaScript)
    executor.execute(contrato2, calculoPorLinhaJavaScript)


    // Script kotlin ficou lento, e tive que adicionar um adendo no ScriptExecutor
    val calculoPorLinhaKotlin = Rule(type = ScriptLanguage.KOTLIN,script = """
        var custoPorLinha = param("valorPorLinha")
        var linhas = load("LINHA")
        persist(custoPorLinha * linhas.size)
    """.trimIndent())
    executor.execute(contrato1, calculoPorLinhaKotlin)
    executor.execute(contrato2, calculoPorLinhaKotlin)


}

