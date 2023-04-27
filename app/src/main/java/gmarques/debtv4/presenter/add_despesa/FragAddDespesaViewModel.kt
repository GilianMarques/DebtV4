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
import java.lang.Exception

class FragAddDespesaViewModel : ViewModel() {

    var despesaPaga: Boolean = false
    var valorDespesa: String = "0"
    var nomeDespesa: String? = null
    var dataDePagamentoDaDespesa: Long? = null
    var dataEmQueDespesaFoiPaga: Long? = null
    var tipoRecorrencia: Recorrencia.Tipo? = null
    var qtdRepeticoes: Long? = null
    var dataLimiteDaRepeticao: Long? = null
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
        if (!validarDataDePagamento()) return@launch
        if (!validarDataEmQueDespesaFoiPaga()) return@launch
        if (!validarRecorrencia()) return@launch
        if (!validarDataLimiteRecorrencia()) return@launch

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

    /**
     * Se o usuario selecionar a data pelo datapicker, ela sera automaticamente uma data valida, se ele
     * optar por digitar a data, [dataDePagamentoDaDespesa]  será nulo enquanto a data for invalida
     */
    private fun validarDataDePagamento(): Boolean {
        if (dataDePagamentoDaDespesa == null) return erroDeValidacao(context.getString(R.string.Verifique_a_data_de_pagamento_da_despesa))
        return true
    }

    /**
     * Se o usuario selecionar a data pelo datapicker, ela sera automaticamente uma data valida, se ele
     * optar por digitar a data, [dataEmQueDespesaFoiPaga]  será nulo enquanto a data for invalida
     */
    private fun validarDataEmQueDespesaFoiPaga(): Boolean {
        if (despesaPaga && dataEmQueDespesaFoiPaga == null) return erroDeValidacao(context.getString(R.string.Verifique_a_data_em_que_a_despesa_foi_paga))
        return true
    }

    /**
     * Valida o tipo e quantidade de repetiçoes da despesa com base no tipo de recorrencia escolhida.
     * O dialogo que faz a coleta dos dados ja impoe as regras de repetiçao pro
     * usuario, essa verificação é mais por desencargo mesmo.
     *
     * @return true se o usuario nao selecionou nenhum tipo de recorencia, ou se selecionou e o valor
     * das repetiçoes é valido para o tipo de repetiçao
     */
    private fun validarRecorrencia(): Boolean {
        return when (tipoRecorrencia) {
            Recorrencia.Tipo.MESES -> validarRepeticaoMeses()
            Recorrencia.Tipo.DIAS  -> validarRepeticaoDias()
            null                   -> true
        }
    }

    private fun validarRepeticaoMeses(): Boolean {
        return if (qtdRepeticoes == null) throw Exception("A quantidade de repetiçoes nao pode ser nula se o usuario selecionou um tipo de recorrencia. Corrija essa brecha")
        else if (qtdRepeticoes!! < Recorrencia.INTERVALO_MIN_REPETICAO_MESES) erroDeValidacao(context.getString(R.string.Valor_de_repeti_o_menor_que_o_permitido_para_meses))
        else if (qtdRepeticoes!! > Recorrencia.INTERVALO_MAX_REPETICAO_MESES) erroDeValidacao(context.getString(R.string.Valor_de_repeti_o_maior_que_o_permitido_para_meses))
        else true
    }

    private fun validarRepeticaoDias(): Boolean {
        return if (qtdRepeticoes == null) throw Exception("A quantidade de repetiçoes nao pode ser nula se o usuario selecionou um tipo de recorrencia. Corrija essa brecha")
        else if (qtdRepeticoes!! < Recorrencia.INTERVALO_MIN_REPETICAO_DIAS) erroDeValidacao(context.getString(R.string.Valor_de_repeti_o_menor_que_o_permitido_para_dias))
        else if (qtdRepeticoes!! > Recorrencia.INTERVALO_MAX_REPETICAO_DIAS) erroDeValidacao(context.getString(R.string.Valor_de_repeti_o_maior_que_o_permitido_para_dias))
        else true
    }

    // TODO: joga exception?
    private fun validarDataLimiteRecorrencia(): Boolean {

        // usuario esqueceu de selecionar a data limite da recorrencia
        return if (tipoRecorrencia != null && dataLimiteDaRepeticao == null) erroDeValidacao(context.getString(R.string.Selecione_a_data_limite_da_recorr_ncia))
        else if (dataLimiteDaRepeticao == Recorrencia.LIMITE_RECORRENCIA_INDEFINIDO) true
        else true
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