package com.snapp.domain.repository

import com.snapp.data.model.layout.LayoutConfig

interface LayoutRepository {
    suspend fun getLayout(): LayoutConfig
}
