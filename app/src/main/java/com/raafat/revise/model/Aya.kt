package com.raafat.revise.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
@Immutable
data class Aya(
    @SerializedName("id")
    @PrimaryKey val id: Int,
    @SerializedName("aya_no")
    val ayaNo: Int,
    @SerializedName("aya_text")
    val ayaText: String,
    @SerializedName("page")
    val page: Int,
    @SerializedName("sura_no")
    val sora: Int
)