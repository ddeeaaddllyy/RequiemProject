package com.application.requiemproject

import android.app.Application
import com.application.requiemproject.di.AppContainer

class App : Application() {
    val container by lazy { AppContainer(this) }
}
