package com.snapp.domain.usecase.layout

import com.snapp.data.model.layout.LayoutConfig
import com.snapp.domain.repository.LayoutRepository

class GetLayoutUseCase(
    private val layoutRepository: LayoutRepository
) {
    suspend operator fun invoke(): LayoutConfig = layoutRepository.getLayout()
}
