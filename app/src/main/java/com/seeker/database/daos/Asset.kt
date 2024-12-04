package com.seeker.database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.seeker.database.entities.AssetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(assetEntity: AssetEntity)

    @Delete
    suspend fun delete(assetEntity: AssetEntity)

    @Update
    suspend fun update(assetEntity: AssetEntity)

    @Query("SELECT * FROM AssetEntity")
    fun getAllAssets(): Flow<List<AssetEntity>>
}