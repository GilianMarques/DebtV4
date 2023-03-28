package gmarques.debtv4

import android.app.Application

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        inst = this
    }

    companion object {
        lateinit var inst: App
    }
}