package gmarques.debtv4.domain.usecases.despesas

import gmarques.debtv4.data.Mapper
import gmarques.debtv4.data.firebase.cloud_firestore.DespesaDaoFireBase
import gmarques.debtv4.data.firebase.cloud_firestore.RecorrenciaDaoFireBase
import gmarques.debtv4.data.room.dao.DespesaDaoRoom
import gmarques.debtv4.data.room.dao.RecorrenciaDaoRoom
import gmarques.debtv4.domain.entidades.Despesa
import gmarques.debtv4.domain.entidades.Recorrencia
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import javax.inject.Inject

/**
 * @Author: Gilian Marques
 * @Date: sábado, 23 de maio de 2023 às 16:39
 */
class AtualizarRecorrenciasDaDespesaUsecase @Inject constructor(
    private val despesaDaoRoom: DespesaDaoRoom,
    private val despesaDaoFirebase: DespesaDaoFireBase,
    private val recorrenciaDaoRoom: RecorrenciaDaoRoom,
    private val recorrenciaDaoFirebase: RecorrenciaDaoFireBase,
    private val mapper: Mapper,
) {

    /**
     * atualiza a versao recorrente e as copias da despesa originalmente editada pelo usuario, esta,
     * deve ser atualizada atraves de [AtualizarDespesaUsecase] primeiro para entao ter seus relativos
     * atualizados neste usecase aqui.
     *
     * Esse usecase insere nos objetos apenas as alteraçoes que o usuario fez, preservando as diferenças
     * de cada objeto como na versao 3 do Debt.
     */
    suspend operator fun invoke(recorrencia: Recorrencia?, copias: List<Despesa>, alteracoes: HashMap<String, Any>) {

        recorrencia?.let { atualizarRecorrencia(recorrencia, alteracoes) }
        atualizarCopias(copias, alteracoes)
    }

    private suspend fun atualizarRecorrencia(dr: Recorrencia, alteracoes: java.util.HashMap<String, Any>) {

        val json = mapper.emJson(dr)

        json.keys().forEach { if (alteracoes.contains(it)) json.put(it, alteracoes[it]) }

        val recorrenciaAtualizada = mapper.getObjeto(json.toString(), Recorrencia::class.java)
        recorrenciaAtualizada.ultimaAtualizacao = DateTime(DateTimeZone.UTC).millis

        val entidade = mapper.getRecorrenciaEntidade(recorrenciaAtualizada)

        recorrenciaDaoFirebase.addOuAtualizar(entidade)
        recorrenciaDaoRoom.addOuAtualizar(entidade)
    }

    private suspend fun atualizarCopias(copias: List<Despesa>, alteracoes: HashMap<String, Any>) =
        copias.forEach { copia ->

            val json = mapper.emJson(copia)

            json.keys().forEach { if (alteracoes.contains(it)) json.put(it, alteracoes[it]) }

            val despesaAtualizada = mapper.getObjeto(json.toString(), Despesa::class.java)

            ajustarAtualizacoes(despesaAtualizada, copia)

            despesaAtualizada.ultimaAtualizacao = DateTime(DateTimeZone.UTC).millis
            val entidade = mapper.getDespesaEntidade(despesaAtualizada)

            despesaDaoFirebase.addOuAtualizar(entidade)
            despesaDaoRoom.addOuAtualizar(entidade)
        }

    /**
     * Algumas alteraçoes que o usuario fez na despesa nao devem ser propagadas para suas copias
     * outras devem ser ajustadas de acordo com cada copia. Essa função executa essa tarefa.
     */
    private fun ajustarAtualizacoes(despesaAtualizada: Despesa, despesaOriginal: Despesa) {

        //certas atualizaçoes nao devem ser propagadas para as copias da despesa editada
        despesaAtualizada.estaPaga = despesaOriginal.estaPaga
        despesaAtualizada.dataEmQueFoiPaga = despesaOriginal.dataEmQueFoiPaga

        // essa atualização deve ser adaptada para cada copia, de forma que só o dia do pagamento seja afetado
        val usuarioAlterouADataDePagamentoDaDespesa = despesaAtualizada.dataDoPagamento != despesaOriginal.dataDoPagamento
        if (usuarioAlterouADataDePagamentoDaDespesa) {

            val diaDaDataAtualizada = DateTime(despesaAtualizada.dataDoPagamento, DateTimeZone.UTC).dayOfMonth
            var dataOriginal = DateTime(despesaOriginal.dataDoPagamento, DateTimeZone.UTC)
            val diaCorreto = dataOriginal.dayOfMonth().maximumValue.coerceAtMost(diaDaDataAtualizada)

            dataOriginal = dataOriginal.withDayOfMonth(diaCorreto)

            despesaAtualizada.dataDoPagamento = dataOriginal.millis
        }

    }


}