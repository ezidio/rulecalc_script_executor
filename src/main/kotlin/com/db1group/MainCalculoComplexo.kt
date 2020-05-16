package com.db1group

import com.db1group.rule.RuleExecutor
import com.db1group.rule.RuleLanguage
import com.db1group.contract.BilledDataRepository
import com.db1group.contract.ContractVersion
import com.db1group.rule.Rule
import java.math.BigDecimal
import java.util.*

fun main() {
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

    val calculoComplexo = Rule(
        type = RuleLanguage.PYTHON, script = """
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
}