package com.climaster.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_feedback_table")
data class UserFeedbackEntity(
    @PrimaryKey val id: String,
    val sensation: String, // Enum-a String bezala gordeko dugu
    val recordedTemp: Double,
    val recordedHumidity: Int,
    val timestamp: Long
)