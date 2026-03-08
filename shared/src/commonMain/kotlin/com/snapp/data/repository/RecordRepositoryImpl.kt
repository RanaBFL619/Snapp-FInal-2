package com.snapp.data.repository

import com.snapp.data.api.SnappApiClient
import com.snapp.data.model.record.DataDeleteRequest
import com.snapp.data.model.record.DataInsertRequest
import com.snapp.data.model.record.DataUpdateRequest
import com.snapp.domain.repository.RecordRepository
import kotlinx.serialization.json.JsonObject

class RecordRepositoryImpl(
    private val api: SnappApiClient
) : RecordRepository {

    override suspend fun getRecord(id: String): JsonObject = api.getRecord(id)

    override suspend fun createRecord(request: DataInsertRequest): JsonObject = api.insertData(request)

    override suspend fun updateRecord(request: DataUpdateRequest): JsonObject = api.updateData(request)

    override suspend fun deleteRecord(request: DataDeleteRequest) {
        api.deleteData(request)
    }
}
