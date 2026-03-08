package com.snapp.data.repository

import com.snapp.data.api.SnappApiClient
import com.snapp.data.model.page.PageConfig
import com.snapp.domain.repository.PageRepository

class PageRepositoryImpl(
    private val api: SnappApiClient
) : PageRepository {

    override suspend fun getPageConfig(slug: String): PageConfig = api.getPageConfig(slug)
}
