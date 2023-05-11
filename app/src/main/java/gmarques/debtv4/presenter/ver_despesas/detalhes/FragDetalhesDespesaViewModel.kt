package gmarques.debtv4.presenter.ver_despesas.detalhes

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import gmarques.debtv4.data.room.dao.DespesaDaoRoom
import gmarques.debtv4.domain.entidades.Despesa
import gmarques.debtv4.domain.usecases.ObservarDespesasUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * ViewModel responsável por gerenciar a fragmento que exibe as despesas.
 */
@HiltViewModel
class FragDetalhesDespesaViewModel @Inject constructor(
) : ViewModel() {

 lateinit var despesa: Despesa
}
