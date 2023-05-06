package gmarques.debtv4.domain

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import gmarques.debtv4.domain.entidades.Periodo
import gmarques.debtv4.domain.extension_functions.Datas.Companion.finalDoMes
import gmarques.debtv4.domain.extension_functions.Datas.Companion.inicioDoMes
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

/**
 * Objeto responsável por controlar o período de meses.
 */
@Suppress("ObjectPropertyName")
object MesesController {

    private val data = MutableLiveData(DateTime.now(DateTimeZone.UTC))

    private val _periodoAtual = MutableLiveData<Periodo>()
    val periodoAtual: LiveData<Periodo>
        get() = _periodoAtual

    init {
        observarData()
    }


    /**
     * Função  que observa alteraçoes na data caudas por outras funçoes e cria um objeto
     * [Periodo] corespondente.
     */
    private fun observarData() {
        data.observeForever {
            _periodoAtual.value = Periodo(data.value!!.inicioDoMes().millis, data.value!!.finalDoMes().millis)
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
