package gmarques.debtv4

import android.app.Application
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        inst = this
    }
// TODO: ajustar as colorOnPrimary, secondary e accent


    companion object {
        lateinit var inst: App
        var demonstracao = true // se true, o app nao mostra valores
    }
}