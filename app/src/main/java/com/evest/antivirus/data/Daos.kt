package com.evest.antivirus.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProtectionEventDao {
    @Insert
    suspend fun insert(event: ProtectionEvent): Long

    @Query("SELECT * FROM protection_events ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<ProtectionEvent>>

    @Query("SELECT * FROM protection_events ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLast(): ProtectionEvent?

    @Query("DELETE FROM protection_events")
    suspend fun clearAll()
}

@Dao
interface ThreatDao {
    @Insert
    suspend fun insert(threat: ThreatEntity): Long

    @Query("SELECT * FROM threats ORDER BY detectedAt DESC")
    fun observeAll(): Flow<List<ThreatEntity>>

    @Query("SELECT * FROM threats WHERE status = 'NEW' ORDER BY detectedAt DESC")
    fun observeActive(): Flow<List<ThreatEntity>>

    @Query("SELECT COUNT(*) FROM threats WHERE status = 'NEW'")
    suspend fun countActive(): Int

    @Update
    suspend fun update(threat: ThreatEntity)

    @Query("SELECT * FROM threats WHERE id = :id")
    suspend fun getById(id: Long): ThreatEntity?
}
