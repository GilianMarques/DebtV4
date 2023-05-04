package gmarques.debtv4.presenter.add_despesa

import android.animation.ValueAnimator
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.view.animation.PathInterpolatorCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.AndroidEntryPoint
import gmarques.debtv4.R
import gmarques.debtv4.databinding.FragAddDespesaBinding
import gmarques.debtv4.domain.entidades.Despesa
import gmarques.debtv4.domain.entidades.Despesa.Companion.VALOR_MINIMO
import gmarques.debtv4.domain.entidades.DespesaRecorrente
import gmarques.debtv4.domain.entidades.DespesaRecorrente.Companion.LIMITE_RECORRENCIA_INDEFINIDO
import gmarques.debtv4.domain.extension_functions.Datas.Companion.converterDDMMAAAAparaMillis
import gmarques.debtv4.domain.extension_functions.Datas.Companion.converterMMAAAAparaMillis
import gmarques.debtv4.domain.extension_functions.Datas.Companion.formatarString
import gmarques.debtv4.domain.extension_functions.Datas.Mascaras.*
import gmarques.debtv4.domain.extension_functions.ExtensionFunctions.Companion.emDouble
import gmarques.debtv4.domain.extension_functions.ExtensionFunctions.Companion.emMoedaSemSimbolo
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
import java.util.Currency
import java.util.Locale
import kotlin.math.abs
import kotlin.random.Random

@AndroidEntryPoint
class FragAddDespesa : CustomFrag() {

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
        initAppBar()
        this.initToolbar(binding, getString(R.string.Nova_despesa))
        initAnimacaoDosCantosDoScrollView()
        initAnimacaoDeCorDaStatusBar()
        initCampoValor()
        initCampoDeNome()
        initCampoData()
        initCampoObservacoes()
        initCampoRepetir()
        initCampoDataLimiteRepeticao()
        initSwitchDespesaPaga()
        initCampoDataEmQueDespesaFoiPaga()
        initBtnConcluir()
        observarErros()
        observarFecharFragmento()
        initDebug()
    }

    // TODO: remover
    private fun initDebug() = lifecycleScope.launch {
        delay(300)
        binding.edtNome.setText("Despesa #${Random.nextInt(9999)}")
        binding.dataPagamento.setText("10/04/2023")
        binding.edtRepetir.requestFocus()
    }

    private fun observarFecharFragmento() {
        viewModel.fecharFragmento.observe(viewLifecycleOwner) {
            if (it) findNavController().navigateUp()
        }
    }

    private fun observarErros() {
        viewModel.msgErro.observe(viewLifecycleOwner) {
            notificarErro(binding.root, it)
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

                mostrarDataPicker(dataInicial) { dataEmUTC: Long, dataFormatada: String ->
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
                binding.dataDespPaga.setText(System.currentTimeMillis().formatarString(DD_MM_AAAA))
                viewModel.dataEmQueDespesaFoiPaga = MaterialDatePicker.todayInUtcMilliseconds()

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

    private fun initCampoRepetir() {
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

    private fun initCampoData() {

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

    private fun mostrarDataPicker(dataInicial: Long, callback: DataPickerCallback) {
        DataPicker(dataInicial, parentFragmentManager, callback)
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
        }
    }

    private fun initCampoObservacoes() {
        binding.tilObservacoes.counterMaxLength = Despesa.COMPRIMENTO_MAXIMO_OBSERVACOES
        binding.observacoes.filters = arrayOf(InputFilter.LengthFilter(Despesa.COMPRIMENTO_MAXIMO_OBSERVACOES))
        binding.observacoes.addTextChangedListener {
            viewModel.observacoesDespesa = it.toString()
        }
    }

    private fun initCampoValor() {
        binding.tvValor.text = VALOR_MINIMO.toString().emMoedaSemSimbolo()
        viewModel.valorDespesa = VALOR_MINIMO.toString()
        binding.tvMoeda.text = Currency.getInstance(Locale.getDefault()).symbol
        binding.tvValor.setOnClickListener {
            TecladoCalculadora.Builder().valorInicial(binding.tvValor.text.toString().emDouble()).callback { valor: String ->

                binding.tvValor.text = valor.emMoedaSemSimbolo()
                viewModel.valorDespesa = valor
            }.titulo(getString(R.string.Digite_o_valor_da_despesa)).build().show(parentFragmentManager, "")

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
        val decorView: View = requireActivity().window.decorView
        val decorViewFlags = decorView.systemUiVisibility

        val targetColor: Int = UIUtils.corAttr(android.R.attr.windowBackground, requireActivity())

        val statusBarColorAnimatior = ValueAnimator.ofArgb(requireActivity().window.statusBarColor, targetColor)
        statusBarColorAnimatior.interpolator = PathInterpolatorCompat.create(1.000f, 0.000f, 1.000f, 1.030f)
        statusBarColorAnimatior.addUpdateListener { animation ->
            requireActivity().window.statusBarColor = animation.animatedValue as Int

            if (animation.animatedFraction < 0.80) {
                decorView.systemUiVisibility = decorViewFlags
            } else {
                decorView.systemUiVisibility = decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }

        animsAtualizadasPeloAppBar.add(statusBarColorAnimatior)
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

}




