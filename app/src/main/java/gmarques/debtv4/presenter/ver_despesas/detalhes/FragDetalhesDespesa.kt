package gmarques.debtv4.presenter.ver_despesas.detalhes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import gmarques.debtv4.databinding.FragDetalhesDespesaBinding
import gmarques.debtv4.domain.entidades.Despesa
import gmarques.debtv4.domain.extension_functions.Datas
import gmarques.debtv4.domain.extension_functions.Datas.Companion.dataFormatada
import gmarques.debtv4.domain.extension_functions.ExtensionFunctions.Companion.emMoeda
import gmarques.debtv4.presenter.main.CustomFrag
import gmarques.debtv4.presenter.outros.UIUtils
import javax.inject.Inject


@AndroidEntryPoint
class FragDetalhesDespesa : CustomFrag() {

    @Inject
    lateinit var graficoDeLinhaComInfo: InitGraficoDelegate

    // para injetar com hilt ao inves de usar @Inject. É assim que se injeta viewModels
    private val viewModel: FragDetalhesDespesaViewModel by viewModels()

    private lateinit var binding: FragDetalhesDespesaBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragDetalhesDespesaBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()

    }

    private fun init() {
        carregarArgumentos()
        popularUi()
        initGrafico()
        initCamposDoGrafico(viewModel.despesa)

    }

    /**
     * Seta os dados da despesa recebida nos campos do grafico
     * Evita codigo duplicado
     */
    private fun initCamposDoGrafico(despesa: Despesa) {
        binding.tvValor.text = despesa.valor.toString().emMoeda()
        binding.tvMes.text = despesa.dataDoPagamento.dataFormatada(Datas.Mascaras.DD_MM_AAAA)
    }

    private fun initGrafico() {

        graficoDeLinhaComInfo.apply {
            lineChart = binding.lineChart
            despesa = viewModel.despesa
            activity = requireActivity()
            clickListener = { despesa: Despesa ->
                UIUtils.vibrar(UIUtils.Vibracao.INTERACAO)
                initCamposDoGrafico(despesa)
            }
        }.executar()
    }


    private fun popularUi() {

        binding.edtNome.setText(viewModel.despesa.nome)

        binding.edtValor.setText(viewModel.despesa.valor.toString().emMoeda())
        binding.edtDataPagamento.setText(viewModel.despesa.dataDoPagamento.dataFormatada(Datas.Mascaras.DD_MM_AAAA))

        // TODO:  binding.edtPaga.setText(if (viewModel.despesa.estaPaga) getString(R.string.Despesa_esta_paga) else getString(R.string.Em_aberto))

        binding.edtObservacoes.setText(viewModel.despesa.observacoes)
        if (viewModel.despesa.observacoes.isEmpty()) binding.edtObservacoes.visibility = View.GONE
    }

    private fun carregarArgumentos() {
        val args: FragDetalhesDespesaArgs = FragDetalhesDespesaArgs.fromBundle(requireArguments())
        viewModel.despesa = args.despesa
    }

}




