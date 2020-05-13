package com.db1group

import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import java.io.StringReader
import java.util.function.Consumer
import java.util.function.Function
import javax.script.ScriptEngineManager

class ScriptExecutor(
    private val repository: BilledDataRepository,
    private val engineManager: ScriptEngineManager = ScriptEngineManager(Thread.currentThread().contextClassLoader)) {

    init {
        setIdeaIoUseFallback() // Isso é para o script do kotlin não confundir com a IDE
    }

    fun execute(contract: ContractVersion, rule: Rule) {
        try {
            val engine = this.engineManager.getEngineByMimeType(rule.type.mediaType)
            val bindings = engine.createBindings()
            bindings["load"] = Function<String, List<BilledData>> { type -> repository.listByType(contract, type) }
            bindings["param"] = Function<String, Double> { param -> contract.parameters[param]?.toDouble() ?: throw Exception("Parametro não existe: $param") }
            bindings["persist"] = Consumer<Double> { value ->  println("Gravando valor $value") }

            // Script Kotlin não é tão simples de trabalhar igual ao JavaScript
            // Tive que adicionar esse "pré-script" para poder deixar as funções iguais no JavaScript
            // Pra POC ficou bom, mas creio ser necessário criar um Strategy pra fazer isso de forma melhor.
            if (rule.type == ScriptLanguage.KOTLIN) {
                engine.eval("""
                    import com.db1group.BilledData
                    import java.math.BigDecimal
                    import java.util.function.Consumer
                    import java.util.function.Function
                    val load = { type:String -> (bindings["load"] as Function<String, List<BilledData>>).apply(type) }
                    val param = { param:String -> (bindings["param"] as Function<String, Double>).apply(param) }
                    val persist = { value:Double -> (bindings["persist"] as Consumer<Double>).accept(value) }
                """.trimIndent(), bindings)
            }

            engine.eval(StringReader(rule.script), bindings)

        } catch (e: Exception) {
            throw Exception("Erro ao executar script", e)
        }
    }



}