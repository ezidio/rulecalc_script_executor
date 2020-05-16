package com.db1group.rule


enum class RuleLanguage(val mediaType:String) {
    KOTLIN("text/x-kotlin"),
    JAVASCRIPT("application/javascript"),
    PYTHON("text/python"),
    MVEL("application/x-mvel")
}