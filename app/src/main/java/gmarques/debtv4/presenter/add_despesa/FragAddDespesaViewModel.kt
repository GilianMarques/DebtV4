package gmarques.debtv4.presenter.add_despesa

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class FragAddDespesaViewModel : ViewModel() {

    private val _usuarioLogado: MutableLiveData<Boolean> = MutableLiveData()
    val usuarioLogado get() = _usuarioLogado


    fun usuarioLogado() = FirebaseAuth.getInstance().currentUser != null


}