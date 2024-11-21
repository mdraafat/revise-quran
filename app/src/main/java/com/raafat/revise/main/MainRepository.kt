package com.raafat.revise.main

import android.app.Application
import androidx.annotation.WorkerThread
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.raafat.revise.model.Aya
import com.raafat.revise.persistence.AyaDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

class MainRepository @Inject constructor(
    private val application: Application,
    private val ayaDao: AyaDao
) {

    @WorkerThread
    fun loadAyaList() = flow {
        val ayaList = ayaDao.getAyaList()
        if (ayaList.isEmpty()) {
            val json = loadJsonFromAssets("uthmanic_hafs.json")
            val parsedAyaList = parseJson(json)
            ayaDao.insertAyaList(parsedAyaList)
            emit(parsedAyaList)
        }else{
            emit(ayaList)
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun loadJsonFromAssets(fileName: String): String = withContext(Dispatchers.IO) {
        val inputStream = application.applicationContext.assets.open(fileName)
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        return@withContext bufferedReader.use { it.readText() }
    }

    private fun parseJson(json: String): List<Aya> {
        val gson = Gson()
        val ayaListType = object : TypeToken<List<Aya>>() {}.type
        return gson.fromJson(json, ayaListType)
    }
}