package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AdminReportEntity
import com.example.data.model.ChatEntity
import com.example.data.model.FriendEntity
import com.example.data.model.MessageEntity
import com.example.data.model.StatusStoryEntity
import com.example.data.model.UserSessionEntity

@Database(
    entities = [
        UserSessionEntity::class,
        ChatEntity::class,
        MessageEntity::class,
        FriendEntity::class,
        StatusStoryEntity::class,
        AdminReportEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class DrakzoDatabase : RoomDatabase() {
    abstract fun drakzoDao(): DrakzoDao

    companion object {
        @Volatile
        private var INSTANCE: DrakzoDatabase? = null

        fun getDatabase(context: Context): DrakzoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DrakzoDatabase::class.java,
                    "drakzo_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
