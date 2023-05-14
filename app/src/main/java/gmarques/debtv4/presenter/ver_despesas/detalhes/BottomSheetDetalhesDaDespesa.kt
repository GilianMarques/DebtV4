package gmarques.debtv4.presenter.ver_despesas.detalhes

import android.view.View
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import gmarques.debtv4.App
import gmarques.debtv4.R
import gmarques.debtv4.databinding.BsDetalhesDespesaBinding
import gmarques.debtv4.domain.PeriodosController
import gmarques.debtv4.domain.entidades.Despesa
import gmarques.debtv4.domain.entidades.DespesaRecorrente
import gmarques.debtv4.domain.extension_functions.Datas
import gmarques.debtv4.domain.extension_functions.Datas.Companion.dataFormatada
import gmarques.debtv4.domain.extension_functions.ExtensionFunctions.Companion.emMoeda
import gmarques.debtv4.domain.extension_functions.ExtensionFunctions.Companion.formatarHtml
import gmarques.debtv4.domain.usecases.despesas.AtualizarDespesasUsecase
import gmarques.debtv4.domain.usecases.despesas.GetDespesasPorNomeNoPeriodoUseCase
import gmarques.debtv4.domain.usecases.despesas.RemoverDespesasUseCase
import gmarques.debtv4.domain.usecases.despesas_recorrentes.GetDespesaRecorrenteUseCase
import gmarques.debtv4.domain.usecases.despesas_recorrentes.RemoverDespesaRecorrenteUseCase
import gmarques.debtv4.presenter.main.CustomFrag
import gmarques.debtv4.presenter.outros.UIUtils
import gmarques.debtv4.presenter.pop_ups.CustomBottomSheet
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Days


class BottomSheetDetalhesDaDespesa(
    private val despesa: Despesa,
    private val fragmento: CustomFrag,
) {
    // TODO: obter versao recorrente  pra remover se o usuario aprovar
    // TODO: exibir dialogo, remover despesas e inicializar os outros botoes

    // TODO: terminar esse dialogo e colocar o db na memoria

    private lateinit var atualizarDespesasUsecase: AtualizarDespesasUsecase
    private lateinit var removerDespesaRecorrenteUseCase: RemoverDespesaRecorrenteUseCase
    private lateinit var getDespesaRecorrenteUseCase: GetDespesaRecorrenteUseCase
    private lateinit var getDespesasPorNomeNoPeriodoUseCase: GetDespesasPorNomeNoPeriodoUseCase
    private lateinit var removerDespesaUsecase: RemoverDespesasUseCase
    private lateinit var initGraficoDelegate: InitGraficoDelegate

    private var dialogo: CustomBottomSheet = CustomBottomSheet()
    private var binding: BsDetalhesDespesaBinding = BsDetalhesDespesaBinding.inflate(fragmento.layoutInflater)

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface Dependencias {
        fun getDelegate(): InitGraficoDelegate
        fun getRemoverDespesaUsecase(): RemoverDespesasUseCase
        fun getDespesasPorNomeNoPeriodoUseCase(): GetDespesasPorNomeNoPeriodoUseCase
        fun getDespesaRecorrenteUseCase(): GetDespesaRecorrenteUseCase
        fun removerDespesaRecorrenteUseCase(): RemoverDespesaRecorrenteUseCase
        fun atualizarDespesasUsecase(): AtualizarDespesasUsecase
    }


    /**
     * Inicializa os componentes da tela de detalhes da despesa.
     * Carrega os argumentos recebidos, inicializa o gráfico, preenche os campos com os dados da despesa
     * (nome, valor, data de pagamento), inicializa o campo de exibição do estado da despesa,
     * e inicializa o campo de observações.
     */
    private fun init() {
        initDependencias()
        initGrafico()
        initCamposDoGrafico(despesa)
        initNome()
        initValor()
        initDataDePagamento()
        initCampoDespesaPaga()
        initObservacoes()
        initBotaoEditar()
        initBotaoPagar()
        initBotaoRemover()
    }

    private fun initDependencias() {

        val entryPoint = EntryPoints.get(App.inst, Dependencias::class.java)

        initGraficoDelegate = entryPoint.getDelegate()
        removerDespesaUsecase = entryPoint.getRemoverDespesaUsecase()
        getDespesasPorNomeNoPeriodoUseCase = entryPoint.getDespesasPorNomeNoPeriodoUseCase()
        getDespesaRecorrenteUseCase = entryPoint.getDespesaRecorrenteUseCase()
        removerDespesaRecorrenteUseCase = entryPoint.removerDespesaRecorrenteUseCase()
        atualizarDespesasUsecase = entryPoint.atualizarDespesasUsecase()
    }

    /**
     * Inicializa o campo de exibição do estado da despesa.
     * O campo exibirá informações sobre o estado da despesa, como se foi paga, a data de pagamento,
     * ou quantos dias faltam para o vencimento, se estiver em aberto ou atrasada.
     */
    private fun initCampoDespesaPaga() {

        val edtEstado = binding.edtEstado

        if (despesa.estaPaga) {
            val formattedText = fragmento.getString(
                R.string.Foi_paga_em_x,
                despesa.dataEmQueFoiPaga!!.dataFormatada(Datas.Mascaras.DD_MM_AAAA)
            )
            edtEstado.setText(formattedText)
        } else {
            val diasAteVencer = Days.daysBetween(
                DateTime.now(DateTimeZone.UTC),
                DateTime(despesa.dataDoPagamento, DateTimeZone.UTC)
            ).days

            val statusText = when {
                diasAteVencer in 1..3 -> fragmento.getString(R.string.Vence_em_x_dias, diasAteVencer) // vence em até 3 dias
                diasAteVencer == 0    -> fragmento.getString(R.string.Vence_hoje)
                diasAteVencer < 0     -> fragmento.getString(R.string.Atrasada_a_x_dias, diasAteVencer * -1)
                else                  -> fragmento.getString(R.string.Em_aberto)
            }
            edtEstado.setText(statusText)
        }

    }

    /**
     * Seta os dados da despesa recebida nos campos do grafico
     * Evita codigo duplicado
     */
    private fun initCamposDoGrafico(despesa: Despesa) {
        binding.tvValor.text = despesa.valor.toString().emMoeda()
        binding.tvMes.text = despesa.dataDoPagamento.dataFormatada(Datas.Mascaras.DD_MM_AAAA)
    }

    /**
     * Inicializa o gráfico.
     */
    private fun initGrafico() {
        initGraficoDelegate.apply {
            lineChart = binding.lineChart
            despesa = this@BottomSheetDetalhesDaDespesa.despesa
            activity = fragmento.requireActivity()
            clickListener = { despesa: Despesa ->
                UIUtils.vibrar(UIUtils.Vibracao.INTERACAO)
                initCamposDoGrafico(despesa)
            }
        }.executar()
    }

    /**
     * Inicializa o campo de nome da despesa.
     */
    private fun initNome() {
        binding.edtNome.setText(despesa.nome)
    }

    /**
     * Inicializa o campo de valor da despesa.
     */
    private fun initValor() {
        binding.edtValor.setText(despesa.valor.toString().emMoeda())
    }

    /**
     * Inicializa o campo de data de pagamento da despesa.
     */
    private fun initDataDePagamento() {
        binding.edtDataPagamento.setText(despesa.dataDoPagamento.dataFormatada(Datas.Mascaras.DD_MM_AAAA))
    }

    /**
     * Inicializa o campo de observações da despesa.
     * Verifica se a observação está vazia e define a visibilidade do campo de acordo.
     */
    private fun initObservacoes() {
        binding.edtObservacoes.setText(despesa.observacoes)
        if (despesa.observacoes.isEmpty()) binding.edtObservacoes.visibility = View.GONE
    }

    private fun initBotaoRemover() {

        binding.btnRemover.setOnClickListener { mostrarDialogoRemoverDespesa() }

    }

    private fun mostrarDialogoRemoverDespesa() {
        val msg = String.format(
            fragmento.getString(R.string.Deseja_mesmo_remover_x_essa_acao_nao_podera_ser_desfeita),
            despesa.nome)
            .formatarHtml()

        MaterialAlertDialogBuilder(fragmento.requireContext()).setTitle(fragmento.getString(R.string.Por_favor_confirme))
            .setMessage(msg)
            .setPositiveButton(fragmento.getString(R.string.Remover)) { _, _ ->

                fragmento.lifecycleScope.launch {
                    removerDespesaUsecase(despesa)
                    verificarRecorrencias()
                }

            }.setNegativeButton(fragmento.getString(R.string.Cancelar)) { _, _ -> }
            .setCancelable(false)
            .show()
    }

    /**
     * Se a despesa for recorrente, o usuario sera indagado se deseja ou nao remover as recorrencias
     * da despesa, se nao o dialogo sera fechado
     */
    private suspend fun verificarRecorrencias() {
        val recorrencias = getDespesasPorNomeNoPeriodoUseCase(despesa.nome, despesa.dataDoPagamento, PeriodosController.periodoMaximo)
        val despesaRecorrente = getDespesaRecorrenteUseCase(despesa)
        if (recorrencias.isEmpty() && despesaRecorrente == null) dialogo.dismiss()
        else mostrarDialogoRemoverRecorrencias(recorrencias, despesaRecorrente)
    }

    private fun mostrarDialogoRemoverRecorrencias(recorrencias: List<Despesa>, despesaRecorrente: DespesaRecorrente?) {
        val nomeMes = Datas.nomeDoMes(despesa.dataDoPagamento)
        val msg = String.format(
            fragmento.getString(R.string.X_eh_uma_despesa_recorrente_deseja_remover_todas_as_copias_de_y_em_diante),
            despesa.nome,
            nomeMes)
            .formatarHtml()

        MaterialAlertDialogBuilder(fragmento.requireContext()).setTitle(fragmento.getString(R.string.Por_favor_confirme))
            .setMessage(msg)
            .setPositiveButton(String.format(fragmento.getString(R.string.De_x_em_diante), nomeMes)) { _, _ ->

                fragmento.lifecycleScope.launch {

                    if (despesaRecorrente != null) removerDespesaRecorrenteUseCase(despesaRecorrente)
                    recorrencias.forEach { removerDespesaUsecase(it) }
                    dialogo.dismiss()
                }

            }
            .setNegativeButton(String.format(fragmento.getString(R.string.De_x_apenas), nomeMes)) { _, _ -> dialogo.dismiss() }
            .setCancelable(false)
            .show()
    }

    private fun initBotaoPagar() {
        binding.btnPagar.setOnClickListener {

            despesa.estaPaga = !despesa.estaPaga

            if (despesa.estaPaga) despesa.dataEmQueFoiPaga = DateTime(DateTimeZone.UTC).millis
            else despesa.dataEmQueFoiPaga = 0

            fragmento.lifecycleScope.launch { atualizarDespesasUsecase(despesa) }
            initCampoDespesaPaga()
        }
    }

    private fun initBotaoEditar() {
        // TODO: implementar
    }


    fun mostrar() {
        init()
        dialogo.customView(binding.root)
            .cancelavel(true)
            .show(fragmento.parentFragmentManager)
    }
}