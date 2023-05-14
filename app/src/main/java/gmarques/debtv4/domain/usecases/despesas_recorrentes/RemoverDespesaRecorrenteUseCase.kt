package gmarques.debtv4.domain.usecases.despesas_recorrentes

import gmarques.debtv4.data.Mapper
import gmarques.debtv4.data.firebase.cloud_firestore.DespesaRecorrenteDaoFireBase
import gmarques.debtv4.data.room.dao.DespesaRecorrenteDaoRoom
import gmarques.debtv4.domain.entidades.DespesaRecorrente
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import javax.inject.Inject

// para injetar dependencias que sao interfaces, é necessario criar modulos para ensinar ao hilt como instanciar um objeto

class RemoverDespesaRecorrenteUseCase @Inject constructor(
    private val roomDao: DespesaRecorrenteDaoRoom,
    private val despesaDaoFirebase: DespesaRecorrenteDaoFireBase,
    private val mapper: Mapper,
) {

    suspend operator fun invoke(despesaRecorrente: DespesaRecorrente) {
        despesaRecorrente.foiRemovida = true
        despesaRecorrente.ultimaAtualizacao = DateTime(DateTimeZone.UTC).millis

        val entidade = mapper.getDespesaRecorrenteEntidade(despesaRecorrente)

        roomDao.addOuAtualizar(entidade)
        despesaDaoFirebase.addOuAtualizar(entidade)
    }

}