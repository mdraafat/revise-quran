package com.raafat.revise.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import com.raafat.revise.model.Aya

@Database(entities = [Aya::class], version = 2, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {

    abstract fun ayaDao(): AyaDao
}