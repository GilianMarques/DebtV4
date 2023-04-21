package gmarques.debtv4.presenter.add_despesa

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import gmarques.debtv4.App
import gmarques.debtv4.R
import gmarques.debtv4.domain.entidades.Despesa
import gmarques.debtv4.domain.entidades.Despesa.Companion.COMPRIMENTO_MAXIMO_NOME
import gmarques.debtv4.domain.entidades.Despesa.Companion.VALOR_MAXIMO
import gmarques.debtv4.domain.entidades.Despesa.Companion.VALOR_MINIMO
import gmarques.debtv4.domain.entidades.Recorrencia
import gmarques.debtv4.domain.extension_functions.ExtensionFunctions.Companion.emMoeda
import kotlinx.coroutines.launch

class FragAddDespesaViewModel : ViewModel() {

    var valorDespesa: String = "0"
    var nomeDespesa: String? = null
    var dataDePagamentoDaDespesaUTC: Long? = null
    var dataEmQueDespesaFoiPagaUTC: Long? = null
    var tipoRecorrencia: Recorrencia.Tipo? = null
    var qtdRepeticoes: Int? = null
    var dataLimiteDaRepeticaoUTC: Long? = null
    var observacoes: String? = null
    private val context = App.inst

    val recorrencia = Recorrencia()
    val despesa = Despesa()

    private val _msgErro: MutableLiveData<String> = MutableLiveData()
    val msgErro get() = _msgErro

    private val _viewComErro: MutableLiveData<View> = MutableLiveData()
    val viewComErro get() = _viewComErro


    fun usuarioLogado() = FirebaseAuth.getInstance().currentUser != null

    fun validarEntradasDoUsuario() = viewModelScope.launch {
        if (!validarValor()) return@launch
        if (!validarNome()) return@launch

    }

    private fun validarValor(): Boolean {
        if (valorDespesa.toFloat() > VALOR_MAXIMO) return erroDeValidacao(String.format(context.getString(R.string.O_valor_da_despesa_nao_pode_ser_maior_que_x), VALOR_MAXIMO.toString().emMoeda()))
        if (valorDespesa.toFloat() < VALOR_MINIMO) return erroDeValidacao(String.format(context.getString(R.string.O_valor_da_despesa_nao_pode_ser_menor_que_x), VALOR_MINIMO.toString().emMoeda()))
        return true
    }

    /**
     * A validaçao do nome é feita na propria UI assim que a view perde o foco
     * Logo basta verificar aqui se o nome nao é nulo. Por precaução verifica-se tambem o
     * comprimento embora este seja limitado direto na interface
     */
    private fun validarNome(): Boolean {

        if (nomeDespesa.isNullOrEmpty()) return erroDeValidacao(R.string.O_nome_nao_pode_ficar_vazio)

        if (nomeDespesa!!.length > COMPRIMENTO_MAXIMO_NOME) return erroDeValidacao(String.format(context.getString(R.string.O_nome_nao_pode_ser_maior_que_x), COMPRIMENTO_MAXIMO_NOME))

        return true
    }

    private fun validarDataDePagamento(): Boolean {
        if (dataDePagamentoDaDespesaUTC == null) return erroDeValidacao(context.getString(R.string.Verifique_a_data_de_pagamento_da_despesa))
        return true
    }

    private fun validarRecorrencia(): Boolean {
        when (tipoRecorrencia) {
            Recorrencia.Tipo.MESES -> TODO()
            Recorrencia.Tipo.DIAS  -> TODO()
            null                   -> TODO()
// TODO: continuar aqui
        }
        return true
    }

    private fun validarDataLimiteRecorrencia(): Boolean {
        return true
    }

    /**
     * Tem o proposito de evitar codigo duplicado
     * @param msgRef deve ser uma referencia à uma string
     */
    private fun erroDeValidacao(msgRef: Int): Boolean {
        return erroDeValidacao(context.getString(msgRef))
    }

    /**
     * Sobrecarga de [erroDeValidacao] para quando nao for possivel passar apenas uma
     * referencia a string desejada
     * @return false, para garantir que a validaçõa seja interrompida assim que
     * o primeiro erro é encontrado
     */
    private fun erroDeValidacao(msg: String): Boolean {
        msgErro.postValue(msg)
        return false
    }
}