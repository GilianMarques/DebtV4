package gmarques.debtv4.presenter.add_despesa

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import gmarques.debtv4.domain.entidades.Despesa
import gmarques.debtv4.domain.entidades.Recorrencia

class FragAddDespesaViewModel : ViewModel() {
    
    val recorrencia = Recorrencia()
    val despesa = Despesa()
    
    var tipoRecorrencia: Recorrencia.Tipo? = null
    var qtdRepeticoes: Int = -1
    
    private val _usuarioLogado: MutableLiveData<Boolean> = MutableLiveData()
    val usuarioLogado get() = _usuarioLogado
    
    
    fun usuarioLogado() = FirebaseAuth.getInstance().currentUser != null
    
    
}