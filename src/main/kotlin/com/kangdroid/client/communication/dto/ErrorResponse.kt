package com.kangdroid.client.communication.dto

class ErrorResponse(
    val errorMessage: String = "",
    var statusCode: String = "",
    var statusMessage: String = ""
)