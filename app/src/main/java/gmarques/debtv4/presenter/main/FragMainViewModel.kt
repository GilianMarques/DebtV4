package gmarques.debtv4.presenter.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class FragMainViewModel : ViewModel() {

    private val _usuarioLogado: MutableLiveData<Boolean> = MutableLiveData()
    val usuarioLogado get() = _usuarioLogado

    /**
     * Essa variavel garante que o splashScreen só será exibido na primeira vez que o
     * Fragmento for exibido
     * Se verdadeiro o splashScreen deve ser coulto imediatamente no FragMain
     * @see FragMain
     */
    var ocultarSplashScreen = false


    fun usuarioLogado() = FirebaseAuth.getInstance().currentUser != null
}