package com.db1group

import java.math.BigDecimal
import java.util.*
import javax.script.ScriptEngineManager

fun main(args: Array<String>) {

    ScriptEngineManager(Thread.currentThread().contextClassLoader).engineFactories.forEach {
        println(it.mimeTypes)
    }

    val contrato1 = ContractVersion(
        customer = UUID.fromString("0d048e90-07fb-4678-85b2-dab31fae281c"),
        parameters = mapOf("valorPorLinha" to BigDecimal(2))
    )

    val contrato2 = ContractVersion(
        customer = UUID.fromString("0d048e90-07fb-4678-85b2-dab31fae281c"),
        parameters = mapOf("valorPorLinha" to BigDecimal(2.5))
    )
    val executor = ScriptExecutor(BilledDataRepository())

    val calculoPorLinhaPython = Rule(
        type = ScriptLanguage.PYTHON, script = """
        custoPorLinha = context.param("valorPorLinha")
        linhas = context.load("LINHA")
        context.persist(custoPorLinha * linhas.size())
    """.trimIndent()
    )
    executor.execute(contrato1, calculoPorLinhaPython)
    executor.execute(contrato2, calculoPorLinhaPython)

    val calculoPorLinhaJavaScript = Rule(
        type = ScriptLanguage.JAVASCRIPT, script = """
        var custoPorLinha = context.param("valorPorLinha");
        var linhas = context.load("LINHA");
        context.persist(linhas.length * custoPorLinha);
    """.trimIndent()
    )
    executor.execute(contrato1, calculoPorLinhaJavaScript)
    executor.execute(contrato2, calculoPorLinhaJavaScript)


    // Script kotlin ficou lento, e tive que adicionar um adendo no ScriptExecutor
    val calculoPorLinhaKotlin = Rule(
        type = ScriptLanguage.KOTLIN, script = """
        var custoPorLinha = context.param("valorPorLinha")
        var linhas = context.load("LINHA")
        context.persist(custoPorLinha * linhas.size)
    """.trimIndent()
    )
    executor.execute(contrato1, calculoPorLinhaKotlin)
    executor.execute(contrato2, calculoPorLinhaKotlin)


}

