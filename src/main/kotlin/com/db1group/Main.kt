package com.db1group

import com.db1group.rule.RuleExecutor
import com.db1group.rule.RuleLanguage
import com.db1group.contract.BilledDataRepository
import com.db1group.contract.ContractVersion
import com.db1group.rule.Rule
import java.math.BigDecimal
import java.util.*
import javax.script.ScriptEngineManager

fun main(args: Array<String>) {

    ScriptEngineManager(Thread.currentThread().contextClassLoader).engineFactories.forEach {
        println(it.mimeTypes)
    }
    val executor = RuleExecutor(BilledDataRepository())

    val contrato1 = ContractVersion(
        customer = UUID.fromString("0d048e90-07fb-4678-85b2-dab31fae281c"),
        parameters = mapOf(
            "valorPorLinha" to BigDecimal(2),
            "percentualVendasAte50000" to BigDecimal(0.05),
            "percentualVendasAte100000" to BigDecimal(0.04),
            "percentualVendasApos10000" to BigDecimal(0.03)
        )
    )

    val contrato2 = ContractVersion(
        customer = UUID.fromString("0d048e90-07fb-4678-85b2-dab31fae281c"),
        parameters = mapOf("valorPorLinha" to BigDecimal(2.5))
    )

    val calculoPorLinhaMVEL = Rule(
        type = RuleLanguage.MVEL, script = """
        custoPorLinha = context.param("valorPorLinha");
        linhas = context.load("LINHA");
        context.persist(linhas.size() * custoPorLinha);
    """.trimIndent()
    )
    executor.execute(contrato1, calculoPorLinhaMVEL)
    executor.execute(contrato2, calculoPorLinhaMVEL)

    val calculoPorLinhaPython = Rule(
        type = RuleLanguage.PYTHON, script = """
        custoPorLinha = context.param("valorPorLinha")
        linhas = context.load("LINHA")
        context.persist(custoPorLinha * linhas.size())
    """.trimIndent()
    )
    executor.execute(contrato1, calculoPorLinhaPython)
    executor.execute(contrato2, calculoPorLinhaPython)

    val calculoPorLinhaJavaScript = Rule(
        type = RuleLanguage.JAVASCRIPT, script = """
        var custoPorLinha = context.param("valorPorLinha");
        var linhas = context.load("LINHA");
        context.persist(linhas.length * custoPorLinha);
    """.trimIndent()
    )
    executor.execute(contrato1, calculoPorLinhaJavaScript)
    executor.execute(contrato2, calculoPorLinhaJavaScript)


    // Script kotlin ficou lento, e tive que adicionar um adendo no ScriptExecutor
    val calculoPorLinhaKotlin = Rule(
        type = RuleLanguage.KOTLIN, script = """
        var custoPorLinha = context.param("valorPorLinha")
        var linhas = context.load("LINHA")
        context.persist(custoPorLinha * linhas.size)
    """.trimIndent()
    )
    executor.execute(contrato1, calculoPorLinhaKotlin)
    executor.execute(contrato2, calculoPorLinhaKotlin)


}

