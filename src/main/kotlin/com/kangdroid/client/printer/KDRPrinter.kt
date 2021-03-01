package com.kangdroid.client.printer

object KDRPrinter {
    private val resetColor: String = "\u001B[0m"
    private val redColor: String = "\u001B[31m"
    private val greenColor: String = "\u001B[32m"

    fun printError(message: String) {
        print(redColor)
        print(message)
        println(resetColor)
    }

    fun printNormal(message: String, newLine: Boolean = true) {
        print(greenColor)
        print(message)
        if (newLine) {
            println(resetColor)
        } else {
            print(resetColor)
        }
    }
}