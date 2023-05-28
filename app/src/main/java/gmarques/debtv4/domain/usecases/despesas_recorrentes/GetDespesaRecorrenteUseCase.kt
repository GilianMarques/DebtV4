package gmarques.debtv4.domain.usecases.despesas_recorrentes

import gmarques.debtv4.data.Mapper
import gmarques.debtv4.data.firebase.cloud_firestore.DespesaDaoFireBase
import gmarques.debtv4.data.firebase.cloud_firestore.DespesaRecorrenteDaoFireBase
import gmarques.debtv4.data.room.dao.DespesaDaoRoom
import gmarques.debtv4.data.room.dao.DespesaRecorrenteDaoRoom
import gmarques.debtv4.data.room.entidades.DespesaEntidade
import gmarques.debtv4.domain.entidades.Despesa
import gmarques.debtv4.domain.entidades.DespesaRecorrente
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.transform
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import javax.inject.Inject

// para injetar dependencias que sao interfaces, é necessario criar modulos para ensinar ao hilt como instanciar um objeto
class GetDespesaRecorrenteUseCase @Inject constructor(
    private val roomDao: DespesaRecorrenteDaoRoom,
    private val mapper: Mapper,
) {

    suspend operator fun invoke(nome:String): DespesaRecorrente? {
        val despRec = roomDao.getPorNome(nome) ?: return null
        return mapper.getDespesaRecorrente(despRec)
    }

}