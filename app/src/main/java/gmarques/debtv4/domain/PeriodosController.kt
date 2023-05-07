package gmarques.debtv4.domain

import gmarques.debtv4.domain.entidades.Periodo
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

    private var observarDataJob: Job? = null
    private val data = MutableStateFlow(DateTime.now(DateTimeZone.UTC))

    private val _periodoAtual = MutableStateFlow(Periodo(data.value))
    val periodoAtual: StateFlow<Periodo>
        get() = _periodoAtual

    init {
        observarData()
    }


    /**
     * Função  que observa alteraçoes na data caudas por outras funçoes e cria um objeto
     * [Periodo] corespondente.
     */
    private fun observarData() {
        observarDataJob = CoroutineScope(Main).launch {
            data.collect { _periodoAtual.value = Periodo(data.value) }
        }
    }

    /**
     * Função que avança a data para o próximo mês.
     */
    fun proximoMes() {
        data.value = data.value!!.plusMonths(1)
    }

    /**
     * Função que retrocede a data para o mês anterior.
     */
    fun mesAnterior() {
        data.value = data.value!!.minusMonths(1)
    }

    /**
     * Função que define a data atual como o mês atual.
     */
    fun mesAtual() {
        data.value = DateTime.now(DateTimeZone.UTC)
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
