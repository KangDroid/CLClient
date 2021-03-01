package com.kangdroid.client.printer

import org.junit.jupiter.api.Test

// Those test class is NOT to check whether response value/return value is correct.
class KDRPrinterTest {
    @Test
    fun isNormalPrintingWorksWell() {
        KDRPrinter.printNormal("Hello, World! from Normal, Without line!", false)
        KDRPrinter.printNormal("Hello, World! from Normal!")
    }

    @Test
    fun isErrorPrintingWorlsWell() {
        KDRPrinter.printError("Error Message!")
    }
}