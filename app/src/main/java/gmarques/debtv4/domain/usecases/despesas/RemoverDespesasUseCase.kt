package gmarques.debtv4.domain.usecases.despesas

import gmarques.debtv4.data.Mapper
import gmarques.debtv4.data.firebase.cloud_firestore.DespesaDaoFireBase
import gmarques.debtv4.data.room.dao.DespesaDaoRoom
import gmarques.debtv4.domain.entidades.Despesa
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.transform
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import javax.inject.Inject

// para injetar dependencias que sao interfaces, é necessario criar modulos para ensinar ao hilt como instanciar um objeto

class RemoverDespesasUseCase @Inject constructor(
    private val roomDao: DespesaDaoRoom,
    private val despesaDaoFirebase: DespesaDaoFireBase,
    private val mapper: Mapper,
) {

    suspend operator fun invoke(despesa: Despesa) {

        despesa.foiRemovida = true
        despesa.ultimaAtualizacao = DateTime(DateTimeZone.UTC).millis

        val entidade = mapper.getDespesaEntidade(despesa)

        roomDao.addOuAtualizar(entidade)
        despesaDaoFirebase.addOuAtualizar(entidade)
    }

}