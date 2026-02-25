package com.climaster.domain.repository

import com.climaster.core.util.Resource
import com.climaster.domain.model.AiInsight
import com.climaster.domain.model.Weather
import com.climaster.domain.model.UserThermalFeedback
import kotlinx.coroutines.flow.Flow

interface AgentRepository {
    // Funtzio honek magia guztia biltzen du:
    // Eguraldia + Erabiltzailearen historia hartu -> Aholku pertsonalizatua itzuli
    fun generatePersonalizedInsight(
        weather: Weather,
        userHistory: List<UserThermalFeedback>
    ): Flow<Resource<AiInsight>>
}