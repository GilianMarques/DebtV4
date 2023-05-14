package gmarques.debtv4.domain.usecases.despesas

import android.util.Log
import gmarques.debtv4.data.Mapper
import gmarques.debtv4.data.firebase.cloud_firestore.DespesaDaoFireBase
import gmarques.debtv4.data.firebase.cloud_firestore.DespesaRecorrenteDaoFireBase
import gmarques.debtv4.data.room.dao.DespesaDaoRoom
import gmarques.debtv4.data.room.dao.DespesaRecorrenteDaoRoom
import gmarques.debtv4.domain.entidades.Despesa
import gmarques.debtv4.domain.entidades.DespesaRecorrente
import gmarques.debtv4.domain.entidades.DespesaRecorrente.Companion.LIMITE_RECORRENCIA_INDEFINIDO
import gmarques.debtv4.domain.extension_functions.Datas
import gmarques.debtv4.domain.extension_functions.Datas.Companion.dataFormatada
import gmarques.debtv4.domain.extension_functions.Datas.Companion.finalDoMes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import javax.inject.Inject

/**
 * @Author: Gilian Marques
 * @Date: sábado, 15 de maio de 2023 às 14:18
 */
class AtualizarDespesasUsecase @Inject constructor(
    private val despesaDaoRoom: DespesaDaoRoom,
    private val despesaDaoFirebase: DespesaDaoFireBase,
    private val despesaRecorrenteDaoRoom: DespesaRecorrenteDaoRoom,
    private val despesaRecorrenteDaoFirebase: DespesaRecorrenteDaoFireBase,
    private val mapper: Mapper,
) {


    suspend operator fun invoke(despesa: Despesa) {

        despesa.ultimaAtualizacao = DateTime(DateTimeZone.UTC).millis
        val entidade = mapper.getDespesaEntidade(despesa)

        despesaDaoFirebase.addOuAtualizar(entidade)
        despesaDaoRoom.addOuAtualizar(entidade)
    }


}