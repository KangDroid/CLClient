package com.kangdroid.client.communication.dto

import org.bson.types.ObjectId

/**
 * NodeSaveRequestDto: Used when Admin registers compute node to master server.
 *
 * variables:
 * id for db-specific
 * hostName for nickname of the server
 * hostPort for compute-server's port
 * ipAddress for compute-server's IP Address.
 * [regionName] will be initialized after creating dto.
 */
class NodeSaveRequestDto(
    var id: ObjectId,
    var hostName: String,
    var hostPort: String,
    var ipAddress: String
)