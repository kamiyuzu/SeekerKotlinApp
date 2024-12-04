package com.seeker.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.seeker.database.daos.AssetDao
import com.seeker.database.entities.AssetEntity

@Database(entities = [AssetEntity::class], version = 1)
abstract class AssetDatabase : RoomDatabase(){
    abstract val dao: AssetDao
}