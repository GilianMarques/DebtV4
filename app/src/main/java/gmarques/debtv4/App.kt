package gmarques.debtv4

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// TODO: ajustar as colorOnPrimary, secondary e accent

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        inst = this
    }

    companion object {
        lateinit var inst: App
        var demonstracao = false // se true, o app nao mostra valores
    }
}