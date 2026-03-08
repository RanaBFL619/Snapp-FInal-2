package com.snapp.domain.repository

import com.snapp.data.model.page.PageConfig

interface PageRepository {
    suspend fun getPageConfig(slug: String): PageConfig
}
