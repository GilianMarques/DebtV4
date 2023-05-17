package gmarques.debtv4.domain

import gmarques.debtv4.domain.entidades.DespesaRecorrente
import gmarques.debtv4.domain.entidades.Periodo
import gmarques.debtv4.domain.extension_functions.Datas.Companion.inicioDoMes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

/**
 * Objeto responsável por controlar o período de tempo em que o app trabalha.
 */
@Suppress("ObjectPropertyName")
object PeriodosController {

    // TODO: mudar esse valor para a data mais a frente no banco de dados(?)
    val periodoMaximo: Long = DateTime(DateTimeZone.UTC).plusYears(DespesaRecorrente.DATA_LIMITE_IMPORATACAO).millis

    private val _periodoAtual = MutableStateFlow(Periodo(DateTime(DateTimeZone.UTC)))
    val periodoAtual: StateFlow<Periodo>
        get() = _periodoAtual


    /**
     * Função que avança a data para o próximo mês.
     */
    fun proximoMes() {

        //chamo .inicioDoMes() pra garantir que .plusMonths() nao vai saltar dois meses caso o dia do
        // mes que consta na data seja 31 e o do proximo mes seja 30, o que resultaria no dia 1° no mes subsequente
        val data = DateTime(periodoAtual.value.fim, DateTimeZone.UTC).inicioDoMes().plusMonths(1)

        _periodoAtual.value = Periodo(data)

    }

    /**
     * Função que retrocede a data para o mês anterior.
     */
    fun mesAnterior() {

        val data = DateTime(periodoAtual.value.fim, DateTimeZone.UTC).minusMonths(1)
        _periodoAtual.value = Periodo(data)
    }

    /**
     * Função que define a data atual como o mês atual.
     */
    fun mesAtual() {
        _periodoAtual.value = Periodo(DateTime(DateTimeZone.UTC))
    }

    /**
     * Função que define um periodo personalizado para o app.
     *
     *  Ao definir um periodo personalizado, o valor de [_periodoAtual] vai deixar de ser uma
     * representação do mes contido em [data].
     *
     * @param inicioPeriodo O valor em milissegundos da data de início do período.
     * @param finalPeriodo O valor em milissegundos da data de término do período.
     */
    fun selecionarPeriodo(inicioPeriodo: Long, finalPeriodo: Long) {
        _periodoAtual.value = Periodo(inicioPeriodo, finalPeriodo)

    }
}
