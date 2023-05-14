package gmarques.debtv4.domain.usecases.despesas

import gmarques.debtv4.data.Mapper
import gmarques.debtv4.data.room.dao.DespesaDaoRoom
import gmarques.debtv4.domain.entidades.Despesa
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.transform
import javax.inject.Inject

// para injetar dependencias que sao interfaces, é necessario criar modulos para ensinar ao hilt como instanciar um objeto
/**
 * Observa as despesas dentro de um intervalo de data especifico
 */
class ObservarDespesasPorNomeNoPeriodoUseCase @Inject constructor(
    private val roomDao: DespesaDaoRoom,
    private val mapper: Mapper,
) {
    /**
     * Observa alteraçoes na tabela de despesas, emitindo novos valores apenas quando ocorre a
     * atualização de uma despesa dentro do periodo recebido. Isso é assegurado por
     * [Flow.distinctUntilChanged], ja que o Room nao consegue dicernir se a alteração na tabela
     * foi em uma linha inclusa nma query.
     */
    operator fun invoke(nome: String, inicioPeriodo: Long, finalPeriodo: Long): Flow<ArrayList<Despesa>> {
        return roomDao.observarPorNomeNoPeriodo(nome, inicioPeriodo, finalPeriodo)
            .distinctUntilChanged()
            .transform { lista ->
                val despesas = ArrayList<Despesa>()
                lista.forEach { despesas.add(mapper.getDespesa(it)) }
                this.emit(despesas)
            }
    }

}