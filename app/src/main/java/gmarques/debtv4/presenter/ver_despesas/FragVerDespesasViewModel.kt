package gmarques.debtv4.presenter.ver_despesas

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import gmarques.debtv4.data.room.dao.DespesaDaoRoom
import gmarques.debtv4.domain.entidades.Despesa
import gmarques.debtv4.domain.usecases.despesas.ObservarDespesasNoPeriodoUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * ViewModel responsável por gerenciar a fragmento que exibe as despesas.
 */
@HiltViewModel
class FragVerDespesasViewModel @Inject constructor(
    private val observarDespesasNoPeriodoUseCase: ObservarDespesasNoPeriodoUseCase,
) : ViewModel() {

    /**
     *
     * @return [Flow] contendo um [ArrayList] de [Despesa]s registradas no período delimitado pelos
     * parâmetros [inicioPeriodo] e [fimPeriodo].
     *
     * @param inicioPeriodo timestamp em milissegundos do início do período desejado.
     * @param fimPeriodo timestamp em milissegundos do fim do período desejado.
     */
    fun carregarDespesas(inicioPeriodo: Long, fimPeriodo: Long): Flow<ArrayList<Despesa>> {
        return observarDespesasNoPeriodoUseCase(inicioPeriodo, fimPeriodo)
    }
}
