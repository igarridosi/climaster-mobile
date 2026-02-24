package com.climaster.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.climaster.data.local.UserFeedbackDao
import com.climaster.data.local.UserFeedbackEntity
import com.climaster.domain.model.ThermalSensation
import com.climaster.domain.model.UserThermalFeedback
import com.climaster.domain.repository.UserFeedbackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import javax.inject.Inject

class UserFeedbackRepositoryImpl @Inject constructor(
    private val dao: UserFeedbackDao
) : UserFeedbackRepository {

    override suspend fun submitFeedback(feedback: UserThermalFeedback) {
        val entity = UserFeedbackEntity(
            id = feedback.id,
            sensation = feedback.sensation.name,
            recordedTemp = feedback.actualTemp,
            recordedHumidity = feedback.humidity,
            timestamp = feedback.timestamp.toInstant(ZoneOffset.UTC).toEpochMilli()
        )
        dao.insertFeedback(entity)
        // HEMEN: Etorkizunean SignalR edo API deia egingo dugu backendera bidaltzeko
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getFeedbackHistory(): Flow<List<UserThermalFeedback>> {
        return dao.getAllFeedback().map { list ->
            list.map { entity ->
                UserThermalFeedback(
                    id = entity.id,
                    sensation = ThermalSensation.valueOf(entity.sensation),
                    actualTemp = entity.recordedTemp,
                    humidity = entity.recordedHumidity,
                    timestamp = LocalDateTime.ofInstant(Instant.ofEpochMilli(entity.timestamp), ZoneId.systemDefault())
                )
            }
        }
    }
}