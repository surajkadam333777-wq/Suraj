package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [WardrobeItem::class, SavedLook::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wardrobeDao(): WardrobeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wardrobe_database"
                )
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    val dao = database.wardrobeDao()
                    // Prepopulate wardrobe with minimalist pieces
                    for (item in FashionConstants.DefaultItems) {
                        dao.insertWardrobeItem(item)
                    }
                    
                    // Prepopulate two classic style recommendations so the user has immediate examples
                    dao.insertSavedLook(
                        SavedLook(
                            title = "Classic Smart-Casual",
                            description = "An enduring combination balancing breathable refinement and structured polish. Ideal for art galleries and casual executive dinners.",
                            items = "Off-White Linen Shirt, Charcoal Wool Trousers, Black Handcrafted Loafers",
                            categoryHint = "Smart Casual"
                        )
                    )
                    dao.insertSavedLook(
                        SavedLook(
                            title = "Weekend Minimalist",
                            description = "A pristine and highly comfortable ensemble structured specifically for warm weather or urban leisure.",
                            items = "Classic White Tee, Olive Green Twill Chinos, White Minimalist Sneakers",
                            categoryHint = "Minimalist"
                        )
                    )
                }
            }
        }
    }
}
