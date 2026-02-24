package com.climaster.domain.repository

import com.climaster.domain.model.UserThermalFeedback
import kotlinx.coroutines.flow.Flow

interface UserFeedbackRepository {
    // Erabiltzailearen feedback-a gorde (Lokalean eta Sarean)
    suspend fun submitFeedback(feedback: UserThermalFeedback)

    // Erabiltzailearen historiala lortu (IA entrenatzeko)
    fun getFeedbackHistory(): Flow<List<UserThermalFeedback>>
}