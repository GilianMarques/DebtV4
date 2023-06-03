package gmarques.debtv4.domain.usecases.despesas

import gmarques.debtv4.data.Mapper
import gmarques.debtv4.data.firebase.cloud_firestore.DespesaDaoFireBase
import gmarques.debtv4.data.room.dao.DespesaDaoRoom
import gmarques.debtv4.domain.entidades.Despesa
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import javax.inject.Inject

/**
 * @Author: Gilian Marques
 * @Date: sábado, 15 de maio de 2023 às 14:18
 * Nota: esse Usecase nao verifica quais alteraçoes foram feitas no objeto visto que seu objetivo
 * é atualizar apenas o objeto que o usuario atualizou e nao objetos as demais copias desse objeto,
 * sendo assim basta substituir o versao no db pela versao atualizada pelo usuario.
 */
class AtualizarDespesaUsecase @Inject constructor(
    private val despesaDaoRoom: DespesaDaoRoom,
    private val despesaDaoFirebase: DespesaDaoFireBase,
    private val mapper: Mapper,
) {


    suspend operator fun invoke(despesa: Despesa) {

        despesa.ultimaAtualizacao = DateTime(DateTimeZone.UTC).millis
        val entidade = mapper.getDespesaEntidade(despesa)

        despesaDaoFirebase.addOuAtualizar(entidade)
        despesaDaoRoom.addOuAtualizar(entidade)
    }


}