package com.kangdroid.client.error

enum class FunctionResponse {
    // Success
    SUCCESS,
    EMPTY_LIST,
    // Fail With Reason
    CLIENT_NO_TOKEN,
    SERVER_COMMUNICATION_FAILED_WITH_4XX_5XX,
    SERVER_RESPONSE_OK_BUT_NO_BODY,
    SERVER_RESPONSE_OK_BUT_WRONG_FORMAT,
    WRONG_NUMBER_INPUT
}