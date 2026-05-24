package com.example.data

import kotlinx.coroutines.flow.Flow

class WardrobeRepository(private val wardrobeDao: WardrobeDao) {
    val allWardrobeItems: Flow<List<WardrobeItem>> = wardrobeDao.getAllWardrobeItems()
    val allSavedLooks: Flow<List<SavedLook>> = wardrobeDao.getAllSavedLooks()

    suspend fun insertWardrobeItem(item: WardrobeItem): Long {
        return wardrobeDao.insertWardrobeItem(item)
    }

    suspend fun deleteWardrobeItem(item: WardrobeItem) {
        wardrobeDao.deleteWardrobeItem(item)
    }

    suspend fun deleteWardrobeItemById(id: Int) {
        wardrobeDao.deleteWardrobeItemById(id)
    }

    suspend fun insertSavedLook(look: SavedLook): Long {
        return wardrobeDao.insertSavedLook(look)
    }

    suspend fun deleteSavedLook(look: SavedLook) {
        wardrobeDao.deleteSavedLook(look)
    }

    suspend fun deleteSavedLookById(id: Int) {
        wardrobeDao.deleteSavedLookById(id)
    }
}
