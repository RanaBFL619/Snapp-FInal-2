package com.snapp.domain.repository

import com.snapp.data.model.record.DataDeleteRequest
import com.snapp.data.model.record.DataInsertRequest
import com.snapp.data.model.record.DataUpdateRequest
import kotlinx.serialization.json.JsonObject

interface RecordRepository {
    suspend fun getRecord(id: String): JsonObject
    suspend fun createRecord(request: DataInsertRequest): JsonObject
    suspend fun updateRecord(request: DataUpdateRequest): JsonObject
    suspend fun deleteRecord(request: DataDeleteRequest)
}
