package gmarques.debtv4.domain.usecases.despesas_recorrentes

import gmarques.debtv4.data.Mapper
import gmarques.debtv4.data.firebase.cloud_firestore.RecorrenciaDaoFireBase
import gmarques.debtv4.data.room.dao.RecorrenciaDaoRoom
import gmarques.debtv4.domain.entidades.Recorrencia
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import javax.inject.Inject

// para injetar dependencias que sao interfaces, é necessario criar modulos para ensinar ao hilt como instanciar um objeto

class RemoverRecorrenciaUseCase @Inject constructor(
    private val roomDao: RecorrenciaDaoRoom,
    private val despesaDaoFirebase: RecorrenciaDaoFireBase,
    private val mapper: Mapper,
) {

    suspend operator fun invoke(recorrencia: Recorrencia) {
        recorrencia.foiRemovida = true
        recorrencia.ultimaAtualizacao = DateTime(DateTimeZone.UTC).millis

        val entidade = mapper.getRecorrenciaEntidade(recorrencia)

        roomDao.addOuAtualizar(entidade)
        despesaDaoFirebase.addOuAtualizar(entidade)
    }

}