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

    enum class MainMenuEntry(private val menuName: String) {
        LOGIN("Login"),
        REGISTER("Register"),
        REQUEST_IMAGE("Request Image"),
        LIST_CONTAINER("List Registered Container"),
        RESTART_CONTAINER("Restart Container"),
        EXIT("Exit");

        override fun toString(): String {
            return "${ordinal + 1}. $menuName"
        }
    }

    /**
     * Main Entry starts here!
     */
    @PostConstruct
    fun startMain() {
        if (System.getProperty("kdr.isTesting") == "test") return
        var menuSelection: MainMenuEntry

        if (!serverCommunication.isServerAlive()) {
            KDRPrinter.printError("Cannot connect to server!")
            return
        }

        do {
            menuSelection = printMenu()

            when (menuSelection) {
                MainMenuEntry.LOGIN, MainMenuEntry.REGISTER -> {
                    inputUserCredential()?.let {
                        serverCommunication.login(it, (menuSelection == MainMenuEntry.LOGIN))
                    }
                }

                MainMenuEntry.REQUEST_IMAGE -> {
                    // show Region first
                    if (serverCommunication.showRegion() == FunctionResponse.SUCCESS) {
                        KDRPrinter.printNormal("Input region name to create container: ", false)
                        val regionName: String = inputScanner.nextLine()
                        serverCommunication.createClientContainer(regionName)
                    }
                }

                MainMenuEntry.LIST_CONTAINER -> {
                    serverCommunication.showClientContainer()
                }

                MainMenuEntry.RESTART_CONTAINER -> {
                    if (serverCommunication.showClientContainer() == FunctionResponse.SUCCESS) {
                        KDRPrinter.printNormal("Input number of index to restart: ", false)
                        val index: String = inputScanner.nextLine()
                        convertStringToInt(index)?.let {
                            serverCommunication.restartClientContainer(it)
                        }
                    }
                }

                MainMenuEntry.EXIT -> continue
            }

            waitFor()
        } while (menuSelection != MainMenuEntry.EXIT)
    }

    fun printMenu(): MainMenuEntry {
        clearScreen()
        KDRPrinter.printNormal("KDR-Cloud Menu: ")
        enumValues<MainMenuEntry>().forEach {
            KDRPrinter.printNormal(it.toString())
        }
        KDRPrinter.printNormal("\nInput Menu: ", false)

        val input: String = inputScanner.nextLine()

        val inputInt: Int = convertStringToInt(input) ?: MainMenuEntry.EXIT.ordinal

        return convertIntToEnum(inputInt - 1)
    }

    private fun convertIntToEnum(input: Int): MainMenuEntry {
        enumValues<MainMenuEntry>().forEach {
            if (it.ordinal == input) {
                return it
            }
        }

        KDRPrinter.printError("Unknown number ${input+1}.")
        return MainMenuEntry.EXIT
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

    private fun waitFor() {
        print("Press Enter to continue..")
        inputScanner.nextLine()
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