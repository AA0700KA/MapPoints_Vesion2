package ru.skillbranch.mappoints_vesion2

import android.app.Application
import android.content.Context
import ru.skillbranch.mappoints_vesion2.data.MapRepository

class App : Application() {

    val mapRepository : MapRepository

    companion object {

        private var instance : App? = null

        fun application() : App {
            return instance!!
        }

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }

    }

    init {
        instance = this
        mapRepository = MapRepository()
    }

}