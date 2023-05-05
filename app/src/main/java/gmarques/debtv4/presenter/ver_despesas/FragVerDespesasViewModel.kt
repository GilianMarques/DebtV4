package gmarques.debtv4.presenter.ver_despesas

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import gmarques.debtv4.data.repositorios.DespesaRepository
import gmarques.debtv4.domain.entidades.Despesa
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class FragVerDespesasViewModel @Inject constructor(
    private val despesasRepo: DespesaRepository,
) : ViewModel() {

    private val _msgErro: MutableLiveData<String> = MutableLiveData()

    val msgErro get() = _msgErro
    private val _fecharFragmento: MutableLiveData<Boolean> = MutableLiveData()


    val fecharFragmento get() = _fecharFragmento

    suspend fun carregarDespesas(): Flow<ArrayList<Despesa>> {
        return despesasRepo.observarDespesas()
    }
}
