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
    val executor = ScriptExecutor(BilledDataRepository())


    val calculoComplexo = Rule(
        type = ScriptLanguage.PYTHON, script = """
        import itertools
        
        # Precisa estar ordenado para o groupBy funcionar corretamente
        pedidos = sorted(context.load("PEDIDO"), key = lambda p:p.instance)
        
        # Calcula o total em vendas
        totalEmVendas = sum(map(lambda x: x.metadata["value"], pedidos))
        context.log("Total em vendas {0}".format(totalEmVendas))
        
        # Baseado no total de vendas, define o percentual cobrado
        if totalEmVendas <= 50000:
          percentualVendas = context.param("percentualVendasAte50000")
        elif totalEmVendas <= 100000:
          percentualVendas = context.param("percentualVendasAte100000")
        else:
          percentualVendas = context.param("percentualVendasApos10000")
        context.log("Usando percentual de vendas {0}".format(percentualVendas))
        
        total = 0
        # Agrupa os pedidos por instancia
        pedidosPorInstancia = itertools.groupby(pedidos, lambda o: o.instance)
        
        # Para cada instancia, soma o total em vendas. 
        # Se for menor que 100, usa valor fixo, caso contrario usa percentual
        for instancia, pedidos in pedidosPorInstancia:
          vendasDaInstancia = sum(map(lambda x: x.metadata["value"], list(pedidos)))
          valor = 150 if vendasDaInstancia < 100 else vendasDaInstancia * percentualVendas 
          context.log("Instancia {0} vendeu {1}, custando {2}".format(instancia, vendasDaInstancia, valor))
          total += valor

        # Persiste o total do faturamento
        context.persist(total)
    """.trimIndent()
    )
    executor.execute(contrato1, calculoComplexo)


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

