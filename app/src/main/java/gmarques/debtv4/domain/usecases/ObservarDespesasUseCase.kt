package gmarques.debtv4.domain.usecases

import gmarques.debtv4.data.Mapper
import gmarques.debtv4.data.room.dao.DespesaDaoRoom
import gmarques.debtv4.domain.entidades.Despesa
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform
import javax.inject.Inject

// para injetar dependencias que sao interfaces, é necessario criar modulos para ensinar ao hilt como instanciar um objeto
/**
 * Observa as despesas dentro de um intervalo de data especifico
 */
class ObservarDespesasUseCase @Inject constructor(
    private val roomDao: DespesaDaoRoom,
    private val mapper: Mapper,
) {

    operator fun invoke(inicioPeriodo: Long, finalPeriodo: Long): Flow<ArrayList<Despesa>> {
        return roomDao.observar(inicioPeriodo,finalPeriodo).transform { lista ->
            val despesas = ArrayList<Despesa>()
            lista.forEach { despesas.add(mapper.getDespesa(it)) }
            this.emit(despesas)
        }
    }

}