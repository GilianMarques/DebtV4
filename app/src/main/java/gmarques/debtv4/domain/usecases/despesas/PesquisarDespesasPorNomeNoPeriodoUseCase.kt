package gmarques.debtv4.domain.usecases.despesas

import gmarques.debtv4.data.Mapper
import gmarques.debtv4.data.room.dao.DespesaDaoRoom
import gmarques.debtv4.domain.entidades.Despesa
import javax.inject.Inject

// para injetar dependencias que sao interfaces, é necessario criar modulos para ensinar ao hilt como instanciar um objeto
/**
 * Obtem as despesas dentro de um intervalo de data especifico com base no nome
 */
class PesquisarDespesasPorNomeNoPeriodoUseCase @Inject constructor(
    private val roomDao: DespesaDaoRoom,
    private val mapper: Mapper,
) {

    suspend operator fun invoke(nome: String, inicioPeriodo: Long, finalPeriodo: Long): List<Despesa> {
        return roomDao.pesquisarPorNomeNoPeriodo(nome, inicioPeriodo, finalPeriodo)
            .map { mapper.getDespesa(it) }
    }

}