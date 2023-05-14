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
 * @Date: sábado, 29 de abril de 2023 às 23:49
 */
class AddDespesasUsecase @Inject constructor(
    private val despesaDaoRoom: DespesaDaoRoom,
    private val despesaDaoFirebase: DespesaDaoFireBase,
    private val despesaRecorrenteDaoRoom: DespesaRecorrenteDaoRoom,
    private val despesaRecorrenteDaoFirebase: DespesaRecorrenteDaoFireBase,
    private val mapper: Mapper,
) {

    /**
     * Essa é a data limite que o app permite importações de objetos no futuro. despesas recorrentes
     * nao deverao ser importadas se suas datas excederem esse valor, nesse caso sua versao recorrente
     * deverá ser salva no banco para que os novos meses criados as importem no momento de sua criação
     */
    private val limiteMaximoDoApp = DateTime(DateTimeZone.UTC).plusYears(DespesaRecorrente.DATA_LIMITE_IMPORATACAO)
    private var dataLimiteDaRecorrencia: DateTime? = null

    suspend operator fun invoke(despesa: Despesa, despesaRecorrente: DespesaRecorrente? = null) {
        addOuAtualizarDespesa(despesa)

        if (despesaRecorrente != null) {
            dataLimiteDaRecorrencia = calcularDataLimiteDaRecorrencia(despesaRecorrente)

            if (manterCopiaRecorrente(despesaRecorrente)) {
                addDespesaRecorrente(despesaRecorrente)
            }

            when (despesaRecorrente.tipoDeRecorrencia) {
                DespesaRecorrente.Tipo.MES -> addDespesaRecorrentePorMes(despesa, despesaRecorrente)
                DespesaRecorrente.Tipo.DIA -> addDespesaRecorrentePorDia(despesa, despesaRecorrente)
            }
        }
    }

    private suspend fun addDespesaRecorrentePorDia(despesa: Despesa, despesaRecorrente: DespesaRecorrente) {

        var proxData = DateTime(DateTimeZone.UTC).withMillis(despesa.dataDoPagamento)

        while (true) {
            proxData = proxData.plusDays(despesaRecorrente.intervaloDasRepeticoes)

            if (proxData.isAfter(dataLimiteDaRecorrencia)) break

            val novaDespesa = mapper.clonarDespesa(despesa)
            novaDespesa.dataDoPagamento = proxData.millis
            novaDespesa.estaPaga = false

            addOuAtualizarDespesa(novaDespesa)
            Log.d("USUK", "DespesaController.addDespesaRecorrentePorMes: ${novaDespesa.uid} ${novaDespesa.dataDoPagamento.dataFormatada(Datas.Mascaras.DD_MM_AAAA_H_M_S)}")
        }

    }

    private suspend fun addDespesaRecorrentePorMes(despesa: Despesa, despesaRecorrente: DespesaRecorrente) {

        var proxData = DateTime(DateTimeZone.UTC).withMillis(despesa.dataDoPagamento)
        val diaPgtoDespesa = proxData.dayOfMonth // isolo o dia para futuras verificações

        while (true) {
            /*Ao calcular a proxima data de repetição da despesa, verifico se o dia do pagamento da despesa esta dentro dos limites
        * do mes. Considerando que meses podem ter de 28 a 31 dias, se uma despesa com vencimento no dia 31
        * tiver que ser adicionada em um mes com 28, 29 ou 30 dias o seu dia de pagamento sera decrementado para o ultimo
        * dia desse mes, assim evitando que a despesa acabe caindo no mes seguinte
        * */
            proxData = proxData.plusMonths(despesaRecorrente.intervaloDasRepeticoes).finalDoMes()
            proxData = proxData.withDayOfMonth(diaPgtoDespesa.coerceAtMost(proxData.dayOfMonth))

            if (proxData.isAfter(dataLimiteDaRecorrencia)) break

            val novaDespesa = mapper.clonarDespesa(despesa)
            novaDespesa.dataDoPagamento = proxData.millis
            novaDespesa.estaPaga = false

            addOuAtualizarDespesa(novaDespesa)
            Log.d("USUK", "DespesaController.addDespesaRecorrentePorMes: ${novaDespesa.uid} ${DateTime(DateTimeZone.UTC).withMillis(novaDespesa.dataDoPagamento)}")
        }

    }

    /**
     * Verifica se  o limite da recorrencia dessa despesa caso nao seja [DespesaRecorrente.LIMITE_RECORRENCIA_INDEFINIDO]
     * esta dentro do limite maximo de variação de datas do app
     *
     * @return a data maxima em que a despesa pode ser importada, esta data pode ser a data maxima permitida pelo app
     * ou a data limite de recorrencia da propria despesa, caso esta nao viole os limites e nao seja indefinida
     */
    private fun calcularDataLimiteDaRecorrencia(despesaRecorrente: DespesaRecorrente): DateTime {

        val dataLimiteDaRecorrencia = if (despesaRecorrente.dataLimiteDaRecorrencia == LIMITE_RECORRENCIA_INDEFINIDO) limiteMaximoDoApp
        else if (limiteMaximoDoApp.isAfter(despesaRecorrente.dataLimiteDaRecorrencia)) DateTime(DateTimeZone.UTC).withMillis(despesaRecorrente.dataLimiteDaRecorrencia)
        else limiteMaximoDoApp

        return dataLimiteDaRecorrencia.finalDoMes()
    }

    /**
     * Compara as datas de recorrencia e de limite de importação do app pra verificar se
     * a versao recorrente da despesa deve ser persistida para que seja importada em futuros meses
     * que ainda nao existem
     *
     * @return true se a versao recorrente tiver que ser persistida
     */
    private fun manterCopiaRecorrente(despesaRecorrente: DespesaRecorrente): Boolean {

        if (despesaRecorrente.dataLimiteDaRecorrencia == LIMITE_RECORRENCIA_INDEFINIDO) return true
        val dataLimiteRecorrencia = DateTime(DateTimeZone.UTC).withMillis(despesaRecorrente.dataLimiteDaRecorrencia).finalDoMes()
        return dataLimiteRecorrencia.isAfter(limiteMaximoDoApp)
    }

    /**
     * Adiciona a despesa nos bancos de dados local e da nuvem, não tem problema se por algum
     * motivo o envio da despesa pra nuvem falhar, posteriormente quando o app sincronizar
     * as pendecias serao resolvidas
     */
    private suspend fun addOuAtualizarDespesa(despesa: Despesa) = withContext(Dispatchers.IO) {
        despesa.ultimaAtualizacao = DateTime(DateTimeZone.UTC).millis

        val entidade = mapper.getDespesaEntidade(despesa)
        despesaDaoFirebase.addOuAtualizar(entidade)
        despesaDaoRoom.addOuAtualizar(entidade)
    }

    /**
     * Adiciona a despesa recorrente nos bancos de dados local e da nuvem, não tem problema se por algum
     * motivo o envio da despesa recorrente pra nuvem falhar, posteriormente quando o app sincronizar
     * as pendecias serao resolvidas
     */
    private suspend fun addDespesaRecorrente(despesa: DespesaRecorrente) =
        withContext(Dispatchers.IO) {
            despesa.ultimaAtualizacao = DateTime(DateTimeZone.UTC).millis

            val entidade = mapper.getDespesaRecorrenteEntidade(despesa)
            despesaRecorrenteDaoFirebase.addOuAtualizar(entidade)
            despesaRecorrenteDaoRoom.addOuAtualizar(entidade)
        }

}