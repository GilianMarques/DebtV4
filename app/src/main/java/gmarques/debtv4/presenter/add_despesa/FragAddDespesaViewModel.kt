package gmarques.debtv4.presenter.add_despesa

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import gmarques.debtv4.domain.entidades.Despesa
import gmarques.debtv4.domain.entidades.Recorrencia

class FragAddDespesaViewModel : ViewModel() {
    
    /**
     *  -1 = despesa recorrente
     */
    var dataLimiteDaRepeticao: Long? = null
    var dataDePagamentoDaDespesa: Long? = null
    var dataEmQueDespesaFoiPaga: Long? = null
    var tipoRecorrencia: Recorrencia.Tipo? = null
    var qtdRepeticoes: Int? = null
    
    val recorrencia = Recorrencia()
    val despesa = Despesa()
    
    private val _usuarioLogado: MutableLiveData<Boolean> = MutableLiveData()
    val usuarioLogado get() = _usuarioLogado
    
    
    fun usuarioLogado() = FirebaseAuth.getInstance().currentUser != null
    
    
}