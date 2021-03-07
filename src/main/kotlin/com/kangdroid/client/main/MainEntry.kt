package com.kangdroid.client.main

import com.kangdroid.client.communication.ServerCommunication
import com.kangdroid.client.communication.dto.UserLoginRequestDto
import com.kangdroid.client.error.FunctionResponse
import com.kangdroid.client.printer.KDRPrinter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.Console
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
        if (System.getProperty("kdr.isTesting") == "test") return
        var menuSelection: Int

        if (!serverCommunication.isServerAlive()) {
            KDRPrinter.printError("Cannot connect to server!")
            return
        }

        do {
            menuSelection = printMenu()

            when (menuSelection) {
                1, 2 -> {
                    val userLoginRequestDto: UserLoginRequestDto =
                        inputUserCredential() ?: return
                    if (serverCommunication.login(userLoginRequestDto, (menuSelection == 1)) != FunctionResponse.SUCCESS) {
                        menuSelection = 0
                    }
                }

                3 -> {
                    // show Region first
                    if (serverCommunication.showRegion() != FunctionResponse.SUCCESS) return
                    KDRPrinter.printNormal("Input region name to create container: ", false)
                    val regionName: String = inputScanner.nextLine()
                    serverCommunication.createClientContainer(regionName)
                }

                4 -> {
                    serverCommunication.showClientContainer()
                }

                5 -> {
                    if (serverCommunication.showClientContainer() != FunctionResponse.SUCCESS) return
                    KDRPrinter.printNormal("Input number of index to restart: ", false)
                    val index: String = inputScanner.nextLine()
                    serverCommunication.restartClientContainer(convertStringToInt(index) ?: return)
                }

                else -> {
                    KDRPrinter.printError("Unknown number $menuSelection.")
                    menuSelection = 0
                }
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
        print("\u001B[H\u001B[2J")
        System.out.flush()
    }

    private fun inputUserCredential(): UserLoginRequestDto? {
        val console: Console? = System.console()
        val userLoginRequestDto: UserLoginRequestDto = UserLoginRequestDto("", "")
        return if (console == null) {
            // Console does not exists.
            KDRPrinter.printError("Cannot connect to local console!")
            KDRPrinter.printError("This program needs console to continue!")
            null
        } else {
            userLoginRequestDto.userName = console.readLine("Input ID: ")
            userLoginRequestDto.userPassword = String(console.readPassword("Input Password: " ))
            userLoginRequestDto
        }
    }
}