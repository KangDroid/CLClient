package com.kangdroid.client.communication.dto

class UserInformationResponseDto(
    var userName: String = "",
    var userRole: Set<String> = setOf()
)