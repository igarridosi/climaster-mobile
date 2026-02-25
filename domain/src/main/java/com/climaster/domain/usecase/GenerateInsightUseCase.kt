package com.climaster.domain.usecase

import com.climaster.core.util.Resource
import com.climaster.domain.model.AiInsight
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
    // OHARTU HEMEN ERE AiInsight DAGOELA:
    operator fun invoke(weather: Weather): Flow<Resource<AiInsight>> = flow {
        val history = feedbackRepository.getFeedbackHistory().first().take(5)

        // Orain honek Resource<AiInsight> itzuliko du, interfazeari esker
        agentRepository.generatePersonalizedInsight(weather, history).collect {
            emit(it) // <--- Orain "it" AiInsight izango da eta ez du errorerik emango
        }
    }
}