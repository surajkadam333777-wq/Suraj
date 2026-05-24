package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WardrobeDao {
    // --- Wardrobe Items ---
    @Query("SELECT * FROM wardrobe_items ORDER BY id DESC")
    fun getAllWardrobeItems(): Flow<List<WardrobeItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWardrobeItem(item: WardrobeItem): Long

    @Delete
    suspend fun deleteWardrobeItem(item: WardrobeItem)

    @Query("DELETE FROM wardrobe_items WHERE id = :id")
    suspend fun deleteWardrobeItemById(id: Int)

    // --- Saved Looks ---
    @Query("SELECT * FROM saved_looks ORDER BY timestamp DESC")
    fun getAllSavedLooks(): Flow<List<SavedLook>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedLook(look: SavedLook): Long

    @Delete
    suspend fun deleteSavedLook(look: SavedLook)

    @Query("DELETE FROM saved_looks WHERE id = :id")
    suspend fun deleteSavedLookById(id: Int)
}
