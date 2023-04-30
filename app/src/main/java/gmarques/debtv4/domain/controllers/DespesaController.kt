package gmarques.debtv4.domain.controllers

import android.util.Log
import gmarques.debtv4.data.Mapper
import gmarques.debtv4.data.repositorios.DespesaRecorrenteRepository
import gmarques.debtv4.data.repositorios.DespesaRepository
import gmarques.debtv4.domain.entidades.Despesa
import gmarques.debtv4.domain.entidades.DespesaRecorrente
import gmarques.debtv4.domain.entidades.DespesaRecorrente.Companion.LIMITE_RECORRENCIA_INDEFINIDO
import gmarques.debtv4.domain.extension_functions.Datas.Companion.noUltimoDiaDoMes
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import javax.inject.Inject

/**
 * @Author: Gilian Marques
 * @Date: sábado, 29 de abril de 2023 às 23:49
 */
class DespesaController @Inject constructor(
    private val despesaRepo: DespesaRepository,
    private val despesaRecorrenteRepo: DespesaRecorrenteRepository,
    private val mapper: Mapper,
) {

    /**
     * Essa é a data limite que o app permite importações de objetos no futuro. despesas recorrentes
     * nao deverao ser importadas se suas datas excederem esse valor, nesse caso sua versao recorrente
     * deverá ser salva no banco para que os novos meses criados  as importe no momento de sua criação
     */
    private val limiteMaximoDoApp = DateTime(DateTimeZone.UTC).plusYears(DespesaRecorrente.DATA_LIMITE_IMPORATACAO)

    suspend fun addDespesa(despesa: Despesa, despesaRecorrente: DespesaRecorrente?) {
        despesaRepo.addDespesa(despesa)

        if (despesaRecorrente != null) {
            if (manterCopiaRecorrente(despesaRecorrente)) {
                despesaRecorrenteRepo.addDespesaRecorrente(despesaRecorrente)
            }

            when (despesaRecorrente.tipoDeRecorrencia) {
                DespesaRecorrente.Tipo.MESES -> addDespesaRecorrentePorMes(despesa, despesaRecorrente)
                DespesaRecorrente.Tipo.DIAS  -> addDespesaRecorrentePorDia(despesa, despesaRecorrente)
            }
        }
    }

    private suspend fun addDespesaRecorrentePorDia(despesa: Despesa, despesaRecorrente: DespesaRecorrente) {

        val dataLimiteDaRecorrencia = calcularDataLimiteDaRecorrencia(despesaRecorrente)

        var proxData = DateTime(DateTimeZone.UTC).withMillis(despesa.dataDoPagamento)

        while (true) {
            proxData = proxData.plusDays(despesaRecorrente.intervaloDasRepeticoes)

            if (proxData.isAfter(dataLimiteDaRecorrencia)) break

            val novaDespesa = mapper.clonarDespesa(despesa)
            novaDespesa.dataDoPagamento = proxData.millis
            novaDespesa.estaPaga = false

            despesaRepo.addDespesa(novaDespesa)
            Log.d("USUK", "DespesaController.addDespesaRecorrentePorMes: ${novaDespesa.uid} ${DateTime(DateTimeZone.UTC).withMillis(novaDespesa.dataDoPagamento)}")
        }

    }

    private suspend fun addDespesaRecorrentePorMes(despesa: Despesa, despesaRecorrente: DespesaRecorrente) {


        val dataLimiteDaRecorrencia = calcularDataLimiteDaRecorrencia(despesaRecorrente)

        var proxData = DateTime(DateTimeZone.UTC).withMillis(despesa.dataDoPagamento)
        val diaPgtoDespesa = proxData.dayOfMonth // isolo o dia para futuras verificações

        while (true) {
            /*Ao calcular a proxima data de repetição da despesa, verifico se o dia do pagamento da despesa esta dentro dos limites
        * do mes. Considerando que meses podem ter de 28 a 31 dias, se uma despesa com vencimento no dia 31
        * tiver que ser adicionada em um mes com 28, 29 ou 30 dias o seu dia de pagamento sera decrementado para o ultimo
        * dia desse mes, assim evitando que a despesa acabe caindo no mes seguinte
        * */
            proxData = proxData.plusMonths(despesaRecorrente.intervaloDasRepeticoes).noUltimoDiaDoMes()
            proxData = proxData.withDayOfMonth(diaPgtoDespesa.coerceAtMost(proxData.dayOfMonth))

            if (proxData.isAfter(dataLimiteDaRecorrencia)) break

            val novaDespesa = mapper.clonarDespesa(despesa)
            novaDespesa.dataDoPagamento = proxData.millis
            novaDespesa.estaPaga = false

            despesaRepo.addDespesa(novaDespesa)
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

        return dataLimiteDaRecorrencia.noUltimoDiaDoMes()
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

        return DateTime(DateTimeZone.UTC).withMillis(despesaRecorrente.dataLimiteDaRecorrencia).isAfter(limiteMaximoDoApp)
    }


}