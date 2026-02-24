package com.climaster.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserFeedbackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedback(feedback: UserFeedbackEntity)

    @Query("SELECT * FROM user_feedback_table ORDER BY timestamp DESC")
    fun getAllFeedback(): Flow<List<UserFeedbackEntity>>
}