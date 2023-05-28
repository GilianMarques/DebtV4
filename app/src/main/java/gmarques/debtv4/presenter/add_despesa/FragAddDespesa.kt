package gmarques.debtv4.presenter.add_despesa

import android.animation.ValueAnimator
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import androidx.core.view.WindowCompat
import androidx.core.view.animation.PathInterpolatorCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import gmarques.debtv4.R
import gmarques.debtv4.databinding.FragAddDespesaBinding
import gmarques.debtv4.domain.entidades.Despesa
import gmarques.debtv4.domain.entidades.DespesaRecorrente
import gmarques.debtv4.domain.entidades.DespesaRecorrente.Companion.LIMITE_RECORRENCIA_INDEFINIDO
import gmarques.debtv4.domain.extension_functions.Datas
import gmarques.debtv4.domain.extension_functions.Datas.Companion.converterDDMMAAAAparaMillis
import gmarques.debtv4.domain.extension_functions.Datas.Companion.converterMMAAAAparaMillis
import gmarques.debtv4.domain.extension_functions.Datas.Companion.dataFormatada
import gmarques.debtv4.domain.extension_functions.Datas.Companion.dataFormatadaComOffset
import gmarques.debtv4.domain.extension_functions.Datas.Companion.finalDoMes
import gmarques.debtv4.domain.extension_functions.Datas.Companion.inicioDoMes
import gmarques.debtv4.domain.extension_functions.Datas.Mascaras.*
import gmarques.debtv4.domain.extension_functions.ExtensionFunctions.Companion.emDouble
import gmarques.debtv4.domain.extension_functions.ExtensionFunctions.Companion.emMoedaSemSimbolo
import gmarques.debtv4.domain.extension_functions.ExtensionFunctions.Companion.formatarHtml
import gmarques.debtv4.domain.extension_functions.ExtensionFunctions.Companion.porcentoDe
import gmarques.debtv4.domain.uteis.Nomes
import gmarques.debtv4.presenter.main.CustomFrag
import gmarques.debtv4.presenter.outros.AnimatedClickListener
import gmarques.debtv4.presenter.outros.MascaraData
import gmarques.debtv4.presenter.outros.UIUtils
import gmarques.debtv4.presenter.pop_ups.DataPicker
import gmarques.debtv4.presenter.pop_ups.DataPicker.*
import gmarques.debtv4.presenter.pop_ups.TecladoCalculadora
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.util.Currency
import java.util.Locale
import kotlin.math.abs


@Suppress("UNUSED_ANONYMOUS_PARAMETER")
@AndroidEntryPoint
class FragAddDespesa : CustomFrag() {

    private var corOriginalDoStatusBar: Int = -1

    // para injetar com hilt ao inves de usar @Inject. É assim que se injeta viewModels
    private val viewModel: FragAddDespesaViewModel by viewModels()

    private lateinit var binding: FragAddDespesaBinding
    private val animsAtualizadasPeloAppBar = ArrayList<ValueAnimator>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragAddDespesaBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()

    }

    private fun init() {
        corOriginalDoStatusBar = requireActivity().window.statusBarColor
        initAppBar()
        carregarArgumentos()
        this.initToolbar(binding, if (viewModel.editando) getString(R.string.Editar_despesa) else getString(R.string.Nova_despesa))
        initAnimacaoDosCantosDoScrollView()
        initAnimacaoDeCorDaStatusBar()
        initCampoValor()
        initCampoDeNome()
        initCampoDataPagamento()
        initCampoObservacoes()
        initCampoRepetir()
        initCampoDataLimiteRepeticao()
        initSwitchDespesaPaga()
        initCampoDataEmQueDespesaFoiPaga()
        initBtnConcluir()
        observarErros()
        observarFecharFragmento()
        observarMostrarDialogoRecorrentes()
        popularUi()
    }

    private fun popularUi() = lifecycleScope.launch {
        if (!viewModel.editando) return@launch

        val despesa = viewModel.despesaParaEditar!!

        binding.tvValor.text = despesa.valor.toString().emMoedaSemSimbolo()
        binding.despesaPaga.isChecked = despesa.estaPaga
        binding.edtNome.setText(despesa.nome)
        binding.dataPagamento.setText(despesa.dataDoPagamento.dataFormatadaComOffset(DD_MM_AAAA))

        with(binding.dataDespPaga) {
            delay(200)
            setText(despesa.dataEmQueFoiPaga?.dataFormatadaComOffset(DD_MM_AAAA))
        }

        binding.observacoes.setText(despesa.observacoes)

    }

    private fun carregarArgumentos() {
        val args: FragAddDespesaArgs = FragAddDespesaArgs.fromBundle(requireArguments())
        viewModel.despesaParaEditar = args.despesa
    }

    private fun observarFecharFragmento() = lifecycleScope.launch {
        viewModel.fecharFragmento.collect { value ->
            if (value) findNavController().navigateUp()
        }
    }

    private fun observarErros() = lifecycleScope.launch {
        viewModel.msgErro.collect { value ->
            notificarErro(binding.root, value)
        }
    }

    private fun observarMostrarDialogoRecorrentes() = lifecycleScope.launch {
        viewModel.mostrarDialogoAtualizarRecorrentes.collect { pacote ->
            if (pacote != null) mostrarDialogoRemoverRecorrencias(pacote)
        }
    }

    private fun initBtnConcluir() {

        binding.fabConcluir.setOnClickListener {
            binding.root.clearFocus()
            viewModel.validarEntradasDoUsuario()
        }
    }

    private fun initCampoDataEmQueDespesaFoiPaga() {

        binding.dataDespPaga.addTextChangedListener(MascaraData.mascaraData())

        binding.ivDataPickerDespPaga.setOnClickListener(object : AnimatedClickListener() {
            override fun onClick(view: View) {
                super.onClick(view)

                val dataInicial = viewModel.dataEmQueDespesaFoiPaga
                    ?: MaterialDatePicker.todayInUtcMilliseconds()

                mostrarDataPickerQuandoDespesaFoiPaga(dataInicial) { _: Long, dataFormatada: String ->
                    binding.dataDespPaga.setText(dataFormatada)
                    binding.dataDespPaga.setSelection(dataFormatada.length)
                }
            }
        })

        binding.dataDespPaga.addTextChangedListener {
            // o valor setado será null até que seja digitada uma data valida
            viewModel.dataEmQueDespesaFoiPaga = it.toString().converterDDMMAAAAparaMillis()
        }
    }

    private fun initSwitchDespesaPaga() {
        binding.despesaPaga.setOnCheckedChangeListener { _: CompoundButton, checado: Boolean ->

            viewModel.despesaPaga = checado

            if (checado) {
                binding.containerDataDespesaPaga.visibility = VISIBLE

                if (viewModel.editando) {
                    binding.dataDespPaga.setText(viewModel.despesaParaEditar!!.dataEmQueFoiPaga?.dataFormatada(DD_MM_AAAA))
                    viewModel.dataEmQueDespesaFoiPaga = viewModel.despesaParaEditar!!.dataEmQueFoiPaga
                } else {
                    binding.dataDespPaga.setText(System.currentTimeMillis().dataFormatada(DD_MM_AAAA))
                    viewModel.dataEmQueDespesaFoiPaga = MaterialDatePicker.todayInUtcMilliseconds()
                }

            } else {
                binding.containerDataDespesaPaga.visibility = GONE
                binding.dataDespPaga.setText("")
                viewModel.dataEmQueDespesaFoiPaga = null
            }
        }
    }

    private fun initCampoDataLimiteRepeticao() {

        val compMaximMascara = 7
        val indeterm = getString(R.string.Indeterminadamente)
        binding.ivRecorrente.setOnClickListener {
            binding.dataLimiteRepetir.filters = arrayOf(InputFilter.LengthFilter(indeterm.length))

            binding.dataLimiteRepetir.setText(indeterm)
            binding.dataLimiteRepetir.clearFocus()
        }

        binding.dataLimiteRepetir.addTextChangedListener {


            if (indeterm == it.toString()) {
                viewModel.dataLimiteDaRepeticao = LIMITE_RECORRENCIA_INDEFINIDO
            } else {
                // o valor setado será null até que seja digitada uma data valida
                viewModel.dataLimiteDaRepeticao = it.toString().converterMMAAAAparaMillis()
            }
            if (it.toString().length <= compMaximMascara) binding.dataLimiteRepetir.filters = arrayOf(InputFilter.LengthFilter(compMaximMascara))
        }

        binding.dataLimiteRepetir.addTextChangedListener(MascaraData.mascaraDataMeseAno())

    }

    /**
     * Inicializa ou oculta o campo de repetiçoes
     */
    private fun initCampoRepetir() {

        if (viewModel.editando) {
            binding.repetir.visibility = GONE
            return
        }

        binding.edtRepetir.setOnFocusChangeListener { _: View, b: Boolean ->
            if (b) mostrarBottomSheetRepetir { intervaloDasRepeticoes: Int?, tipoDeRecorrencia: DespesaRecorrente.Tipo?, dica: String ->

                viewModel.intervaloDasRepeticoes = intervaloDasRepeticoes
                viewModel.tipoDeRecorrencia = tipoDeRecorrencia

                if (tipoDeRecorrencia != null) despesaSeRepete(dica)
                else despesaNaoSeRepete(dica)

            }
        }
    }

    /**
     * Limpa a interface e impede que o usuario defina uma data limete para a repetiçao desativando a
     * view que coleta essa informação
     */
    private fun despesaNaoSeRepete(dica: String) {

        binding.clDataLimiteRepetir.visibility = GONE

        binding.dataLimiteRepetir.setText("")

        binding.edtRepetir.setText(dica)
        binding.edtRepetir.clearFocus()

    }

    /**
     * Permite que o usuario selecione a data limite da repeticao desbloqueando a view que coleta essa
     * informação e dando foco nela, além de atualizar a interface com dados da repeticao
     */
    private fun despesaSeRepete(dica: String) {

        binding.clDataLimiteRepetir.visibility = VISIBLE
        binding.dataLimiteRepetir.requestFocus()

        binding.edtRepetir.setText(dica)
    }

    /**
     * Mostra um BottomSheet para coletar os dados de repeticao da despesa, nele a entrada é
     * coletada e verificada, se tudo estiver certo os valores sao passados via callback, senao
     * o usuario é notificado para corrigir o que for necessario
     */
    private fun mostrarBottomSheetRepetir(callback: BottomSheetRepetir.Callback) {
        BottomSheetRepetir(callback, this@FragAddDespesa, viewModel.intervaloDasRepeticoes
            ?: 1, viewModel.tipoDeRecorrencia ?: DespesaRecorrente.Tipo.MES).mostrar()
    }

    private fun initCampoDataPagamento() {

        binding.dataPagamento.addTextChangedListener(MascaraData.mascaraData())

        binding.ivDataPicker.setOnClickListener(object : AnimatedClickListener() {
            override fun onClick(view: View) {
                super.onClick(view)

                val dataInicial = viewModel.dataDePagamentoDaDespesa
                    ?: MaterialDatePicker.todayInUtcMilliseconds()

                mostrarDataPicker(dataInicial) { _: Long, dataFormatada: String ->

                    binding.dataPagamento.setText(dataFormatada)
                    binding.dataPagamento.setSelection(dataFormatada.length)
                }
            }
        })

        binding.dataPagamento.addTextChangedListener {
            // o valor setado será null até que seja digitada uma data valida
            viewModel.dataDePagamentoDaDespesa = it.toString().converterDDMMAAAAparaMillis()
        }

    }

    /**
     * Mostra o picker de data pro usuario adicionar ou editar a data de pagamento da despesa.
     * Se editando, o usuario nao deve ser capaz de alterar o mes ou ano de pagamento da despesa.
     */
    private fun mostrarDataPicker(dataInicial: Long, callback: DataPickerCallback) {

        // limites de datas do datapicker

        val max = when (viewModel.editando) {
            true  -> DateTime(viewModel.despesaParaEditar!!.dataDoPagamento, DateTimeZone.UTC).finalDoMes().millis
            false -> DateTime.now().plusYears(DespesaRecorrente.DATA_LIMITE_IMPORATACAO).millis
        }

        val min = when (viewModel.editando) {
            true  -> DateTime(viewModel.despesaParaEditar!!.dataDoPagamento, DateTimeZone.UTC).inicioDoMes().millis
            false -> DateTime.now().minusYears(DespesaRecorrente.DATA_LIMITE_IMPORATACAO).millis
        }

        DataPicker(dataInicial,
            min,
            max,
            parentFragmentManager,
            callback)
    }

    /**
     * mostra o datapicker pra seelcionar quando a despesa foi paga, usando os limites de data padrao do app
     */
    private fun mostrarDataPickerQuandoDespesaFoiPaga(dataInicial: Long, callback: DataPickerCallback) {


        val max = DateTime.now().plusYears(DespesaRecorrente.DATA_LIMITE_IMPORATACAO).millis
        val min = DateTime.now().minusYears(DespesaRecorrente.DATA_LIMITE_IMPORATACAO).millis

        DataPicker(dataInicial,
            min,
            max,
            parentFragmentManager,
            callback)
    }

    private fun initCampoDeNome() {
        binding.tilNome.counterMaxLength = Despesa.COMPRIMENTO_MAXIMO_NOME
        binding.edtNome.filters = arrayOf(InputFilter.LengthFilter(Despesa.COMPRIMENTO_MAXIMO_NOME))
        binding.edtNome.setOnFocusChangeListener { _: View, b: Boolean ->
            if (!b) {
                val nomeCorrigido = Nomes.aplicarCorrecao(binding.edtNome.text.toString())
                binding.edtNome.setText(nomeCorrigido)
            }
        }
        binding.edtNome.addTextChangedListener {
            viewModel.nomeDespesa = it.toString()
            if (!viewModel.editando) mostarSugestoesDeDespesa(it.toString())

        }
    }

    /**
     * exibe um drop-down list no campo de nome com sugestoes de despesas pro usuario importar
     */
    private fun mostarSugestoesDeDespesa(nome: String) = lifecycleScope.launch() {

        if (nome.length < 2) return@launch

        val (nomes, despesas) = viewModel.buscarSugestoes(nome)
        val adapter = ArrayAdapter(requireContext(),
            android.R.layout.select_dialog_item,
            nomes)

        binding.edtNome.setAdapter(adapter)

        binding.edtNome.setOnItemClickListener { parent, view, position, id ->
            aplicarDadosDaSugestao(despesas[position])
        }
    }

    /**
     * popula a interface com os dados da despesa selecionada no campo de sugestoes
     * e salva os valores no viewmodel
     */
    private fun aplicarDadosDaSugestao(despesa: Despesa) {

        binding.tvValor.text = despesa.valor.toString().emMoedaSemSimbolo()
        viewModel.valorDespesa = despesa.valor.toString()

        binding.dataPagamento.setText(despesa.dataDoPagamento.dataFormatadaComOffset(DD_MM_AAAA))

        binding.observacoes.setText(despesa.observacoes)

    }

    private fun initCampoObservacoes() {
        binding.tilObservacoes.counterMaxLength = Despesa.COMPRIMENTO_MAXIMO_OBSERVACOES
        binding.observacoes.filters = arrayOf(InputFilter.LengthFilter(Despesa.COMPRIMENTO_MAXIMO_OBSERVACOES))
        binding.observacoes.addTextChangedListener {
            viewModel.observacoesDespesa = it.toString()
        }
    }

    private fun initCampoValor() {
        viewModel.valorDespesa = "0"
        binding.tvValor.text = viewModel.valorDespesa.emMoedaSemSimbolo()
        binding.tvMoeda.text = Currency.getInstance(Locale.getDefault()).symbol
        binding.tvValor.setOnClickListener {
            TecladoCalculadora.Builder().valorInicial(binding.tvValor.text.toString().emDouble()).callback { valor: String ->

                binding.tvValor.text = valor.emMoedaSemSimbolo()
                viewModel.valorDespesa = valor
            }.titulo(getString(R.string.Digite_o_valor_da_despesa)).build().show(parentFragmentManager, "")

        }
        binding.tvValor.addTextChangedListener {
            viewModel.valorDespesa = it.toString().emDouble()
        }
    }

    private fun initAppBar() {

        val tamanhoAppBar = UIUtils.tamanhoTela().second / 3

        binding.appbar.layoutParams.height = tamanhoAppBar
        binding.appbar.invalidate()
        binding.appbar.visibility = VISIBLE


        binding.appbar.addOnOffsetChangedListener { _, vertOffset ->
            val porcentagem = abs(vertOffset).porcentoDe(tamanhoAppBar)
            animsAtualizadasPeloAppBar.forEach {
                it.setCurrentFraction(porcentagem / 100)
            }
        }
    }

    private fun initAnimacaoDeCorDaStatusBar() {
        val corAlvo: Int = UIUtils.corAttr(android.R.attr.windowBackground, requireActivity())

        val statusBarColorAnimator = ValueAnimator.ofArgb(requireActivity().window.statusBarColor, corAlvo)
        statusBarColorAnimator.interpolator = PathInterpolatorCompat.create(1.000f, 0.000f, 1.000f, 1.030f)
        statusBarColorAnimator.addUpdateListener { animation ->
            requireActivity().window.statusBarColor = animation.animatedValue as Int

            val windowInsetsController = WindowCompat.getInsetsController(requireActivity().window, requireActivity().window.decorView)
            windowInsetsController.isAppearanceLightStatusBars = animation.animatedFraction >= 0.80
        }

        animsAtualizadasPeloAppBar.add(statusBarColorAnimator)
    }

    private fun initAnimacaoDosCantosDoScrollView() = binding.nestedScroll.post {

        val drawable = binding.nestedScroll.background as GradientDrawable
        drawable.mutate()
        // pego o raio_X do canto superior esquerdo e uso como parametro para os raios da parte superior do drawable
        val raioEmDp = drawable.cornerRadii?.get(1) ?: 0f

        val cornerAnimation = ValueAnimator.ofFloat(raioEmDp, 0f)
        cornerAnimation.interpolator = PathInterpolatorCompat.create(1.000f, 0.000f, 1.000f, 1.030f)
        cornerAnimation.addUpdateListener { animation ->
            val cornerRadius = animation.animatedValue as Float
            drawable.cornerRadii = floatArrayOf(cornerRadius, cornerRadius, //top left
                cornerRadius, cornerRadius, // top right
                0f, 0f, // bottom right
                0f, 0f // bottom left
            )
        }

        animsAtualizadasPeloAppBar.add(cornerAnimation)
    }

    /**
     * pergunta ao usuario se ele quer atualizar as copias da despesa que acabou de ser atualizada
     * caso esta seja recorrente. Se sim chama a função adequada no viewmodel, se nao fecha o fragmento.
     */
    private fun mostrarDialogoRemoverRecorrencias(pacote: FragAddDespesaViewModel.PacoteRecorrente) {
        val despesa = viewModel.despesaParaEditar!!
        val nomeMes = Datas.nomeDoMes(despesa.dataDoPagamento)

        val msg = String.format(
            getString(R.string.X_eh_uma_despesa_recorrente_deseja_atualizar_todas_as_copias_de_y_em_diante),
            despesa.nome,
            nomeMes)
            .formatarHtml()

        MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.Por_favor_confirme))
            .setMessage(msg)
            .setPositiveButton(String.format(getString(R.string.De_x_em_diante), nomeMes)) { _, _ ->
                viewModel.atualizarDespesasRecorrentes(pacote)
            }
            .setNegativeButton(String.format(getString(R.string.De_x_apenas), nomeMes)) { _, _ -> findNavController().navigateUp() }
            .setCancelable(false)
            .show()
    }

    override fun onStop() {
        requireActivity().window.statusBarColor = corOriginalDoStatusBar
        super.onStop()
    }

}




