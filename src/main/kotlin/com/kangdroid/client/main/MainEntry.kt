package com.kangdroid.client.main

import com.kangdroid.client.communication.ServerCommunication
import com.kangdroid.client.printer.KDRPrinter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*
import javax.annotation.PostConstruct

@Component
class MainEntry {
    @Autowired
    private lateinit var serverCommunication: ServerCommunication

    // Scanner
    val inputScanner: Scanner = Scanner(System.`in`)

    /**
     * Main Entry starts here!
     */
    @PostConstruct
    fun startMain() {
        var menuSelection: Int

        if (!serverCommunication.isServerAlive()) {
            KDRPrinter.printError("Cannot connect to server!")
            return
        }

        do {
            menuSelection = printMenu()

            when (menuSelection) {
            }
        } while (menuSelection != 0)
    }

    fun printMenu(): Int {
        clearScreen()
        KDRPrinter.printNormal("""
            KDR-Cloud Menu:
            1. Login
            2. Register
            3. Request Image
            4. List Registered Container
            5. Restart Container
            0. Exit
            
            Input Menu Number: 
        """.trimIndent(), false)

        val input: String = inputScanner.nextLine()

        return convertStringToInt(input) ?: 0
    }

    private fun convertStringToInt(input: String): Int? {
        return runCatching {
            input.toInt()
        }.onFailure {
            KDRPrinter.printError("$input cannot be changed to number!")
            KDRPrinter.printError("Please check number you input.")
        }.getOrNull() ?: run {
            return null
        }
    }

    private fun clearScreen() {
        print("\u001B[H\u001B[2J");
        System.out.flush();
    }
}