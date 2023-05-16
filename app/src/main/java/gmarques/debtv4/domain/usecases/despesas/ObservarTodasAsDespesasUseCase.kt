package gmarques.debtv4.domain.usecases.despesas

import gmarques.debtv4.data.Mapper
import gmarques.debtv4.data.room.dao.DespesaDaoRoom
import gmarques.debtv4.domain.entidades.Despesa
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.transform
import javax.inject.Inject

// para injetar dependencias que sao interfaces, é necessario criar modulos para ensinar ao hilt como instanciar um objeto

class ObservarTodasAsDespesasUseCase @Inject constructor(
    private val roomDao: DespesaDaoRoom,
    private val mapper: Mapper,
) {

    operator fun invoke(): Flow<ArrayList<Despesa>> {
        return roomDao.observarTodas()
            .transform { lista ->
                val despesas = ArrayList<Despesa>()
                lista.forEach { despesas.add(mapper.getDespesa(it)) }
                this.emit(despesas)
            }
    }

}