package com.raafat.revise.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    mainRepository: MainRepository
) : ViewModel() {
    val ayaListFlow = mainRepository.loadAyaList()

    private val _sliderValue = MutableLiveData<Float>()
    val sliderValue: LiveData<Float> get() = _sliderValue

    private val _isTouching = MutableLiveData<Boolean>()
    val isTouching: LiveData<Boolean> get() = _isTouching

    fun onSliderValueChanged(value: Float) {
        _sliderValue.value = value
    }

    fun onStartTouch() {
        _isTouching.value = true
    }

    fun onStopTouch() {
        _isTouching.value = false
    }

}