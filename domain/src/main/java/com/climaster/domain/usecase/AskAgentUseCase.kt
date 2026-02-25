package com.climaster.domain.usecase

import com.climaster.core.util.Resource
import com.climaster.domain.model.Weather
import com.climaster.domain.repository.AgentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AskAgentUseCase @Inject constructor(
    private val agentRepository: AgentRepository
) {
    operator fun invoke(question: String, weather: Weather): Flow<Resource<String>> {
        return agentRepository.askQuestion(question, weather)
    }
}