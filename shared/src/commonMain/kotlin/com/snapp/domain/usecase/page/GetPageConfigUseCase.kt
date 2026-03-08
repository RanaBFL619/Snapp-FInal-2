package com.snapp.domain.usecase.page

import com.snapp.data.model.page.PageConfig
import com.snapp.domain.repository.PageRepository

class GetPageConfigUseCase(
    private val pageRepository: PageRepository
) {
    suspend operator fun invoke(slug: String): PageConfig = pageRepository.getPageConfig(slug)
}
