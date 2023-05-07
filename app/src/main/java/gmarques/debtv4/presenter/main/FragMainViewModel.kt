package gmarques.debtv4.presenter.main

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import gmarques.debtv4.data.sincronismo.SincAdapterCallbackImpl
import gmarques.debtv4.data.sincronismo.api.UICallback
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FragMainViewModel @Inject constructor(
    private val sincAdapterCallback: SincAdapterCallbackImpl,
) : ViewModel() {


    /**
     * Essa variavel garante que o splashScreen só será exibido na primeira vez que o
     * Fragmento for exibido
     * Se verdadeiro o splashScreen deve ser oculto imediatamente no FragMain
     * @see FragMain
     */
    var ocultarSplashScreen = false


    fun usuarioLogado() = FirebaseAuth.getInstance().currentUser != null

    fun sincronizar() {
        viewModelScope.launch {
            sincAdapterCallback.uiCallback = object : UICallback {
                override fun feito(sucesso: Boolean, msg: String?) {
                    Log.d("USUK", "FragMainViewModel.".plus("feito() sucesso = $sucesso, msg = $msg"))
                }

                override fun status(titulo: String, msg: String) {
                    Log.d("USUK", "FragMainViewModel.".plus("status() titulo = $titulo, msg = $msg"))
                }
            }
            sincAdapterCallback.executar()
        }

    }
}