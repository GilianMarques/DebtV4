package gmarques.debtv4.presenter.add_despesa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import gmarques.debtv4.App
import gmarques.debtv4.R
import gmarques.debtv4.data.Mapper
import gmarques.debtv4.domain.PeriodosController
import gmarques.debtv4.domain.usecases.despesas.AdicionarDespesasUsecase
import gmarques.debtv4.domain.entidades.Despesa
import gmarques.debtv4.domain.entidades.Despesa.Companion.COMPRIMENTO_MAXIMO_NOME
import gmarques.debtv4.domain.entidades.Despesa.Companion.VALOR_MAXIMO
import gmarques.debtv4.domain.entidades.Despesa.Companion.VALOR_MINIMO
import gmarques.debtv4.domain.entidades.DespesaRecorrente
import gmarques.debtv4.domain.extension_functions.Datas
import gmarques.debtv4.domain.extension_functions.Datas.Companion.finalDoMes
import gmarques.debtv4.domain.extension_functions.Datas.Companion.inicioDoMes
import gmarques.debtv4.domain.extension_functions.ExtensionFunctions.Companion.emMoeda
import gmarques.debtv4.domain.usecases.despesas.AtualizarDespesaUsecase
import gmarques.debtv4.domain.usecases.despesas.AtualizarRecorrenciasDaDespesaUsecase
import gmarques.debtv4.domain.usecases.despesas.GetDespesasPorNomeNoPeriodoUseCase
import gmarques.debtv4.domain.usecases.despesas.PesquisarDespesasPorNomeNoPeriodoUseCase
import gmarques.debtv4.domain.usecases.despesas_recorrentes.GetDespesaRecorrenteUseCase
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import javax.inject.Inject

@HiltViewModel
class FragAddDespesaViewModel @Inject constructor(
    private val addDespesaUsecase: AdicionarDespesasUsecase,
    private val pesquisarDespesasPorNomeNoPeriodoUseCase: PesquisarDespesasPorNomeNoPeriodoUseCase,
    private val getDespesasPorNomeNoPeriodoUseCase: GetDespesasPorNomeNoPeriodoUseCase,
    private val getDespesaRecorrenteUseCase: GetDespesaRecorrenteUseCase,
    private val atualizarRecorrenciasDaDespesaUsecase: AtualizarRecorrenciasDaDespesaUsecase,
    private val atualizarDespesaUsecase: AtualizarDespesaUsecase,
    private val mapper: Mapper,
) : ViewModel() {

    private val context = App.inst

    /**
     * O usuario esta editando uma despesa?
     * é inicializado pelo setter de [despesaParaEditar]
     *
     */
    var editando: Boolean = false

    /**
     * Serve para comparar com [despesaParaEditar] em busca de mudanças
     * é inicializado pelo setter de [despesaParaEditar]
     */
    private var despesaOriginal: Despesa? = null

    var despesaParaEditar: Despesa? = null // sera nulo se o usuario estiver adicionando uma despesa
        set(value) {
            field = value

            editando = value != null
            value?.let {
                despesaOriginal = mapper.clonarDespesa(value)
            }

        }

    /**
     * Retem os dados de recorrencia da despesa que sera adicionada pelo usuario, se ele estiver
     * editando ao inves de adicionando uma despesa, esse objeto sera sempre nulo.
     */
    private var despesaRecorrente: DespesaRecorrente? = null

    /**
     * Despesa que sera adicionada se o usuario nao estiver editando uma despesa, se for o caso
     * essa despesa nunca sera inicializada.
     */
    private lateinit var novaDespesa: Despesa

    // essas variavies guardam os valores convertidos que o usuario colocou na interface
    var despesaPaga: Boolean = false
    var valorDespesa: String = "0"
    var nomeDespesa: String? = null
    var dataDePagamentoDaDespesa: Long? = null
    var dataEmQueDespesaFoiPaga: Long? = null
    var tipoDeRecorrencia: DespesaRecorrente.Tipo? = null
    var intervaloDasRepeticoes: Int? = null
    var dataLimiteDaRepeticao: Long? = null
    var observacoesDespesa = ""


    private val _msgErro = MutableSharedFlow<String>()
    val msgErro get() = _msgErro

    /**
     * SharedFlow dispara updates uma unica vez, nao re-dispara se a tela girar. ideal pra snackbars
     */
    private val _fecharFragmento = MutableSharedFlow<Boolean>()
    val fecharFragmento get() = _fecharFragmento

    private val _mostrarDialogoAtualizarRecorrentes = MutableSharedFlow<PacoteRecorrente?>()
    val mostrarDialogoAtualizarRecorrentes get() = _mostrarDialogoAtualizarRecorrentes

    /**
     * Serve para interromper uma busca em andamento quando o usuario altera o nome na UI
     */
    private var jobDeBucaDeSugestoes: Job? = null

    fun validarEntradasDoUsuario() = viewModelScope.launch(IO) {
        if (!validarValor()) return@launch
        if (!validarNome()) return@launch
        if (!validarDuplicata()) return@launch
        if (!validarDataDePagamento()) return@launch
        if (!validarDataEmQueDespesaFoiPaga()) return@launch
        if (!validarRecorrencia()) return@launch
        if (!validarDataLimiteRecorrencia()) return@launch

        when (editando) {
            true  -> atulizarDespesa()
            false -> addDespesa()
        }
    }

    private suspend fun atulizarDespesa() {

        despesaParaEditar!!.apply {

            this.nome = nomeDespesa!!
            this.valor = valorDespesa.toDouble()
            this.dataDoPagamento = dataDePagamentoDaDespesa!!
            this.estaPaga = despesaPaga
            this.observacoes = observacoesDespesa

            dataEmQueFoiPaga?.let { this.dataEmQueFoiPaga = it }

        }


        val (copias, despRecorrente) = verificarRecorrencias()

        if (copias.size == 1 && despRecorrente == null) {
            _fecharFragmento.emit(true)
        } else {
            _mostrarDialogoAtualizarRecorrentes.emit(PacoteRecorrente(copias, despRecorrente))
        }

        atualizarDespesaUsecase(despesaParaEditar!!)

    }

    /**
     * Verifica as recorrências de uma despesa original e retorna uma tupla contendo
     * a lista de despesas copiadas no período e a despesa recorrente correspondente,
     * se existir.
     *
     * A função realiza as seguintes etapas:
     * 1. Obtém um array de cópias da despesa original, incluindo a própria despesa original,
     *    dentro do período máximo definido pelo controlador de períodos.
     * 2. Obtém a despesa recorrente correspondente ao nome da despesa original.
     *
     * @return uma tupla contendo a lista de copias da despesa e a despesa recorrente, se existir.
     */
    private suspend fun verificarRecorrencias(): Pair<List<Despesa>, DespesaRecorrente?> {
        val copias = getDespesasPorNomeNoPeriodoUseCase(despesaOriginal!!.nome, despesaOriginal!!.dataDoPagamento, PeriodosController.periodoMaximo)

        val despesaRecorrente = getDespesaRecorrenteUseCase(despesaOriginal!!.nome)

        return copias to despesaRecorrente
    }


    fun atualizarDespesasRecorrentes(pacote: PacoteRecorrente) = viewModelScope.launch(IO) {
        val alteracoes = extrairAtualizacoes()
        atualizarRecorrenciasDaDespesaUsecase(pacote.despesaRecorrente, pacote.copias, alteracoes)
        _fecharFragmento.emit(true)
    }

    /**
     * Extrai as atualizações realizadas na despesa a ser atualizada em relação à despesa original.
     *
     * @return HashMap contendo as atualizações realizadas na despesa.
     */
    private fun extrairAtualizacoes(): HashMap<String, Any> {
// TODO: criar uma classe que faça isso com qualquer objeto
        val despAtt = mapper.emJson(despesaParaEditar!!)
        val despDesatt = mapper.emJson(despesaOriginal!!)

        val atualizacoes = HashMap<String, Any>()

        despAtt.keys().forEach { chave ->
            val valor = despAtt[chave]
            if (valor != despDesatt[chave]) atualizacoes[chave] = valor
        }
        return atualizacoes
    }

    private suspend fun addDespesa() {

        novaDespesa = Despesa().apply {

            this.nome = nomeDespesa!!
            this.valor = valorDespesa.toDouble()
            this.dataDoPagamento = dataDePagamentoDaDespesa!!
            this.estaPaga = despesaPaga
            this.observacoes = observacoesDespesa

            dataEmQueFoiPaga?.let { this.dataEmQueFoiPaga = it }

        }

        if (tipoDeRecorrencia != null) {
            despesaRecorrente = mapper.getDespesaRecorrente(novaDespesa)
            despesaRecorrente!!.estaPaga = false
            despesaRecorrente!!.intervaloDasRepeticoes = intervaloDasRepeticoes!!
            despesaRecorrente!!.dataLimiteDaRecorrencia = dataLimiteDaRepeticao!!
            despesaRecorrente!!.tipoDeRecorrencia = tipoDeRecorrencia!!
        }

        addDespesaUsecase(novaDespesa, despesaRecorrente)
        fecharFragmento.emit(true)

    }

    private fun validarValor(): Boolean {
        if (valorDespesa.toDouble() > VALOR_MAXIMO) return erroDeValidacao(String.format(context.getString(R.string.O_valor_da_despesa_nao_pode_ser_maior_que_x), VALOR_MAXIMO.toString().emMoeda()))
        if (valorDespesa.toDouble() < VALOR_MINIMO) return erroDeValidacao(String.format(context.getString(R.string.O_valor_da_despesa_nao_pode_ser_menor_que_x), VALOR_MINIMO.toString().emMoeda()))
        return true
    }

    /**
     * A validaçao do nome é feita na propria UI assim que a view perde o foco
     * Logo basta verificar aqui se o nome nao é nulo. Por precaução verifica-se tambem o
     * comprimento embora este seja limitado direto na interface
     */
    private fun validarNome(): Boolean {

        if (nomeDespesa.isNullOrEmpty()) return erroDeValidacao(context.getString(R.string.O_nome_nao_pode_ficar_vazio))

        if (nomeDespesa!!.length > COMPRIMENTO_MAXIMO_NOME) return erroDeValidacao(String.format(context.getString(R.string.O_nome_nao_pode_ser_maior_que_x), COMPRIMENTO_MAXIMO_NOME))

        return true
    }

    private suspend fun validarDuplicata(): Boolean {

        if (editando && despesaParaEditar!!.nome == nomeDespesa) return true


        val duplicata = getDespesasPorNomeNoPeriodoUseCase(nomeDespesa!!, DateTime(dataDePagamentoDaDespesa, DateTimeZone.UTC).inicioDoMes().millis, DateTime(dataDePagamentoDaDespesa, DateTimeZone.UTC).finalDoMes().millis).isNotEmpty()

        if (duplicata) return erroDeValidacao(String.format(context.getString(R.string.Ja_existe_uma_despesa_com_o_esse_nome_em_x), Datas.nomeDoMes(dataDePagamentoDaDespesa!!)))

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
        return when (tipoDeRecorrencia) {
            DespesaRecorrente.Tipo.MES -> validarRepeticaoMeses()
            DespesaRecorrente.Tipo.DIA -> validarRepeticaoDias()
            null                       -> true
        }
    }

    private fun validarRepeticaoMeses(): Boolean {
        return if (intervaloDasRepeticoes == null) throw Exception("O intervalo das repetiçoes nao pode ser nulo se o usuario selecionou um tipo de recorrencia. Corrija essa brecha")
        else if (intervaloDasRepeticoes!! < DespesaRecorrente.INTERVALO_MIN_REPETICAO_MESES) erroDeValidacao(context.getString(R.string.Valor_de_repeti_o_menor_que_o_permitido_para_meses))
        else if (intervaloDasRepeticoes!! > DespesaRecorrente.INTERVALO_MAX_REPETICAO_MESES) erroDeValidacao(context.getString(R.string.Valor_de_repeti_o_maior_que_o_permitido_para_meses))
        else true
    }

    private fun validarRepeticaoDias(): Boolean {
        return if (intervaloDasRepeticoes == null) throw Exception("O intervalo das repetiçoes nao pode ser nulo se o usuario selecionou um tipo de recorrencia. Corrija essa brecha")
        else if (intervaloDasRepeticoes!! < DespesaRecorrente.INTERVALO_MIN_REPETICAO_DIAS) erroDeValidacao(context.getString(R.string.Valor_de_repeti_o_menor_que_o_permitido_para_dias))
        else if (intervaloDasRepeticoes!! > DespesaRecorrente.INTERVALO_MAX_REPETICAO_DIAS) erroDeValidacao(context.getString(R.string.Valor_de_repeti_o_maior_que_o_permitido_para_dias))
        else true
    }

    private fun validarDataLimiteRecorrencia(): Boolean {

        // usuario esqueceu de selecionar a data limite da recorrencia
        return if (tipoDeRecorrencia != null && dataLimiteDaRepeticao == null) erroDeValidacao(context.getString(R.string.Selecione_a_data_limite_da_recorr_ncia))
        else if (dataLimiteDaRepeticao == DespesaRecorrente.LIMITE_RECORRENCIA_INDEFINIDO) true
        else true
    }

    /**
     * Envia uma mensagem de erro para a interface do usuário (UI) e retorna false para interromper a validação ao encontrar o primeiro erro.
     * Essa função é usada para exibir na interface o erro encontrado durante a validação das entradas do usuário.
     * Seu propósito é evitar a duplicação de código.
     *
     * @param msg a mensagem de erro a ser exibida na UI.
     * @return false para garantir a interrupção da validação assim que o primeiro erro for encontrado.
     */
    private fun erroDeValidacao(msg: String): Boolean {
        viewModelScope.launch { msgErro.emit(msg) }
        return false
    }


    /**
     * Busca sugestões de despesas com base no nome fornecido.
     *
     * @param nome O nome usado para buscar sugestões de despesas.
     * @return Um par contendo a lista de sugestões de nomes de despesas e a lista mutável de
     *         objetos Despesa correspondentes.
     */
    suspend fun buscarSugestoes(nome: String): Pair<List<String>, MutableList<Despesa>> {
        jobDeBucaDeSugestoes?.cancel()
        jobDeBucaDeSugestoes = Job()

        val sugestoes = mutableListOf<Despesa>()

        return withContext(IO + jobDeBucaDeSugestoes!!) {
            return@withContext pesquisarDespesasPorNomeNoPeriodoUseCase(nome, 0, PeriodosController.periodoAtual.value.fim).distinctBy { it.nome }.map {
                sugestoes.add(it)
                it.nome
            }
        } to sugestoes
    }

    /**
     * serve pra manter os dados obtidos do db para caso o usuario queira atualizar as copias da despesa
     * que ele acabou de atualizar
     */
    data class PacoteRecorrente(val copias: List<Despesa>, val despesaRecorrente: DespesaRecorrente?)

}