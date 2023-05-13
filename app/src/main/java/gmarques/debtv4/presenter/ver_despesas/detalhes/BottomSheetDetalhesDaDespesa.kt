package gmarques.debtv4.presenter.ver_despesas.detalhes

import android.view.View
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import gmarques.debtv4.App
import gmarques.debtv4.R
import gmarques.debtv4.databinding.BsDetalhesDespesaBinding
import gmarques.debtv4.domain.entidades.Despesa
import gmarques.debtv4.domain.extension_functions.Datas
import gmarques.debtv4.domain.extension_functions.Datas.Companion.dataFormatada
import gmarques.debtv4.domain.extension_functions.ExtensionFunctions.Companion.emMoeda
import gmarques.debtv4.presenter.main.CustomFrag
import gmarques.debtv4.presenter.outros.UIUtils
import gmarques.debtv4.presenter.pop_ups.CustomBottomSheet
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Days


class BottomSheetDetalhesDaDespesa(
    private val despesa: Despesa,
    private val fragmento: CustomFrag,
) {


    lateinit var initGraficoDelegate: InitGraficoDelegate

    private var dialogo: CustomBottomSheet = CustomBottomSheet()
    private var binding: BsDetalhesDespesaBinding = BsDetalhesDespesaBinding.inflate(fragmento.layoutInflater)

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface GraficoDelegateComponent {
        fun getDelegate(): InitGraficoDelegate
    }


    /**
     * Inicializa os componentes da tela de detalhes da despesa.
     * Carrega os argumentos recebidos, inicializa o gráfico, preenche os campos com os dados da despesa
     * (nome, valor, data de pagamento), inicializa o campo de exibição do estado da despesa,
     * e inicializa o campo de observações.
     */
    private fun init() {
        initGraficoDelegate = EntryPoints.get(App.inst, GraficoDelegateComponent::class.java).getDelegate()

        initGrafico()
        initCamposDoGrafico(despesa)
        initNome()
        initValor()
        initDataDePagamento()
        initCampoDespesaPaga()
        initObservacoes()
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


    fun mostrar() {
        init()
        /*se esse dialogo for cancelavel sera necessario definir um dismiss listener para
        * tirar o foco a view de repetir, senao ocorrera um bug toda vez que o usuario fechar
        * o dialogo sem ser pelos botoes(salvar e cancelar) onde é possivel editar o texto da view livremente
        * */
        dialogo.customView(binding.root)
            .cancelavel(true)
            .show(fragmento.parentFragmentManager)
    }
}