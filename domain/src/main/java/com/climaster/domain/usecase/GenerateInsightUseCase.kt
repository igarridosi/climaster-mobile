package com.climaster.domain.usecase

import com.climaster.core.util.Resource
import com.climaster.domain.model.Weather
import com.climaster.domain.repository.AgentRepository
import com.climaster.domain.repository.UserFeedbackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GenerateInsightUseCase @Inject constructor(
    private val agentRepository: AgentRepository,
    private val feedbackRepository: UserFeedbackRepository
) {
    operator fun invoke(weather: Weather): Flow<Resource<String>> = flow {
        // 1. Erabiltzailearen azken feedback-ak lortu (Testuingurua)
        val history = feedbackRepository.getFeedbackHistory().first().take(5) // Azken 5ak nahikoa dira

        // 2. Agentea deitu datu guztiekin
        agentRepository.generatePersonalizedInsight(weather, history).collect {
            emit(it)
        }
    }
}