package com.raafat.revise.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.raafat.revise.model.Aya

@Dao
interface AyaDao{

    @Query("SELECT * FROM Aya")
    suspend fun getAyaList(): List<Aya>

    @Insert(onConflict = OnConflictStrategy.NONE)
    suspend fun insertAyaList(ayaList: List<Aya>)

}