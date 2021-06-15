package ru.skillbranch.mappoints_vesion2.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.skillbranch.mappoints_vesion2.App

class ViewModelFactory(private val app : App) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            return MapViewModel(app.mapRepository) as T
        } else {
            error("Unknown view model class $modelClass")
        }
    }

}