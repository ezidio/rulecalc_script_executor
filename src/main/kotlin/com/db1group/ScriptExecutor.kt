package com.db1group

import org.jetbrains.kotlin.cli.common.environment.setIdeaIoUseFallback
import java.io.StringReader
import java.util.function.Consumer
import java.util.function.Function
import javax.script.ScriptEngineManager

class ScriptExecutor(
    val repository: BilledDataRepository,
    private val engineManager: ScriptEngineManager = ScriptEngineManager(Thread.currentThread().contextClassLoader)) {

    init {
        setIdeaIoUseFallback() // Isso é para o script do kotlin não confundir com a IDE
    }

    fun execute(contract: ContractVersion, rule: Rule) {
        try {
            val engine = this.engineManager.getEngineByMimeType(rule.type.mediaType)
            val bindings = engine.createBindings()
            bindings["context"] = RuleContext(contract, this)

            // Script Kotlin não é tão simples de trabalhar igual ao JavaScript
            // E é mais lento também
            // Tive que adicionar esse "pré-script" para poder deixar as funções iguais no JavaScript
            // Isso porque o kotlin coloca todas as variaveis dentro de uma objeto "bindings"
            // Pra POC ficou bom, mas creio ser necessário criar um Strategy pra fazer isso de forma melhor.
            if (rule.type == ScriptLanguage.KOTLIN) {
                engine.eval("""
                    import com.db1group.RuleContext
                    val context = bindings["context"] as RuleContext
                """.trimIndent(), bindings)
            }

            engine.eval(StringReader(rule.script), bindings)

        } catch (e: Exception) {
            throw Exception("Erro ao executar script", e)
        }
    }



}