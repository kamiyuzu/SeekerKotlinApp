package com.seeker.database.repositories

import com.seeker.database.daos.AssetDao
import com.seeker.database.entities.AssetEntity
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/*
Clean architecture pattern
good for separation of logic
 */

/* creating an interface helps in abstraction and easily we can add or remove
required methods without a hassle.
*/
interface Repository {
    suspend fun insert(studentEntity: AssetEntity)
    suspend fun delete(studentEntity: AssetEntity)
    suspend fun update(studentEntity: AssetEntity)
    suspend fun getAllAssets(): Flow<List<AssetEntity>>
}

class AssetRepositoryImpl(private val dao: AssetDao) : Repository {
    override suspend fun insert(studentEntity: AssetEntity) {
        withContext(IO) {
            dao.insert(studentEntity)
        }
    }

    override suspend fun delete(studentEntity: AssetEntity) {
        withContext(IO) {
            dao.delete(studentEntity)
        }
    }

    override suspend fun update(studentEntity: AssetEntity) {
        withContext(IO) {
            dao.update(studentEntity)
        }
    }

    override suspend fun getAllAssets(): Flow<List<AssetEntity>> {
        return withContext(IO) {
            dao.getAllAssets()
        }
    }
}