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
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.datepicker.MaterialDatePicker
import gmarques.debtv4.R
import gmarques.debtv4.databinding.FragAddDespesaBinding
import gmarques.debtv4.domain.entidades.Despesa
import gmarques.debtv4.domain.entidades.Recorrencia
import gmarques.debtv4.domain.extension_functions.ExtensionFunctions.Companion.emDouble
import gmarques.debtv4.domain.extension_functions.ExtensionFunctions.Companion.emMoeda
import gmarques.debtv4.domain.extension_functions.ExtensionFunctions.Companion.emMoedaSemSimbolo
import gmarques.debtv4.domain.extension_functions.ExtensionFunctions.Companion.porcentoDe
import gmarques.debtv4.domain.uteis.DataUtils.Companion.DD_MM_AAAA
import gmarques.debtv4.domain.uteis.DataUtils.Companion.MM_AAAA
import gmarques.debtv4.domain.uteis.DataUtils.Companion.agoraEmMillis
import gmarques.debtv4.domain.uteis.DataUtils.Companion.corrigirFusoHorario
import gmarques.debtv4.domain.uteis.DataUtils.Companion.dataFormatadaParaLong
import gmarques.debtv4.domain.uteis.DataUtils.Companion.formatarData
import gmarques.debtv4.presenter.TecladoCalculadora
import gmarques.debtv4.presenter.main.CustomFrag
import gmarques.debtv4.presenter.outros.AnimatedClickListener
import gmarques.debtv4.presenter.outros.MascaraData
import gmarques.debtv4.presenter.outros.UIUtils
import java.text.NumberFormat
import java.util.*
import kotlin.math.abs

class FragAddDespesa : CustomFrag() {


    private lateinit var viewModel: FragAddDespesaViewModel
    private lateinit var binding: FragAddDespesaBinding
    private val animsAtualizadasPeloAppBar = ArrayList<ValueAnimator>()

    // TODO: criar teclado calculadora
    // TODO: testar tudo oque foi feito até aqui
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragAddDespesaBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[FragAddDespesaViewModel::class.java]
        init()
    }

    private fun init() {
        initAppBar()
        this.initToolbar(binding, getString(R.string.Nova_despesa))
        initAnimacaoDosCantosDoScrollView()
        initAnimacaoDeCorDaStatusBar()
        initTextViewValoreMoeda()
        initCampoDeNome()
        initCampoData()
        initCampoObservacoes()
        initCampoRepetir()
        initCampoDataLimiteRepeticao()
        initSwitchDespesaPaga()
        initCampoDataEmQueDespesaFoiPaga()
    }

    private fun initCampoDataEmQueDespesaFoiPaga() {

        binding.ivDataPickerDespPaga.setOnClickListener(object : AnimatedClickListener() {
            override fun onClick(view: View) {
                super.onClick(view)

                val dataInicial = viewModel.dataEmQueDespesaFoiPaga ?: agoraEmMillis()

                mostrarDataPicker(dataInicial) { _: Long, dataFormatada: String ->
                    binding.dataDespPaga.setText(dataFormatada)
                    binding.dataDespPaga.setSelection(dataFormatada.length)
                }
            }
        })
        binding.dataDespPaga.addTextChangedListener(MascaraData.mascaraData())
        binding.dataDespPaga.addTextChangedListener {
            // o valor setado será null até que seja digitada uma data valida
            viewModel.dataEmQueDespesaFoiPaga = dataFormatadaParaLong(it.toString(), DD_MM_AAAA)
        }
    }

    private fun initSwitchDespesaPaga() {
        binding.despesaPaga.setOnCheckedChangeListener { _: CompoundButton, checado: Boolean ->

            if (checado) {
                binding.containerDataDespesaPaga.visibility = VISIBLE
                binding.dataDespPaga.setText(formatarData(agoraEmMillis(), DD_MM_AAAA))
                viewModel.dataEmQueDespesaFoiPaga = agoraEmMillis()
            } else {
                binding.containerDataDespesaPaga.visibility = GONE
                binding.dataDespPaga.setText("")
                viewModel.dataEmQueDespesaFoiPaga = null
            }
        }
    }

    private fun initCampoDataLimiteRepeticao() {

        val indeterm = getString(R.string.Indeterminadamente)
        binding.ivRecorrente.setOnClickListener {
            binding.dataLimiteRepetir.setText(indeterm)
            binding.dataLimiteRepetir.clearFocus()
        }

        binding.dataLimiteRepetir.addTextChangedListener {

            if (indeterm == it.toString()) {
                viewModel.dataLimiteDaRepeticao = -1 // recorrente
            } else {
                // o valor setado será null até que seja digitada uma data valida
                viewModel.dataLimiteDaRepeticao = dataFormatadaParaLong(it.toString(), MM_AAAA)
            }
        }

        binding.dataLimiteRepetir.addTextChangedListener(MascaraData.mascaraDataMeseAno())

    }


    private fun initCampoRepetir() {
        binding.edtRepetir.setOnFocusChangeListener { _: View, b: Boolean ->
            if (b) mostrarBottomSheetRepetir { qtdRepeticoes: Int, tipoIntervalo: String, tipoRecorrencia: Recorrencia.Tipo? ->

                // qtdRepeticoes = 0 sempre que o usuario clica em 'nao repetir' no bottomsheet
                if (qtdRepeticoes > 0) {
                    viewModel.qtdRepeticoes = qtdRepeticoes
                    viewModel.tipoRecorrencia = tipoRecorrencia
                    despesaSeRepete(qtdRepeticoes, tipoIntervalo)
                } else {
                    viewModel.qtdRepeticoes = null
                    viewModel.tipoRecorrencia = null
                    despesaNaoSeRepete()
                }
            }
        }
    }

    /**
     * Limpa a interface e impede que o usuario defina uma data limete para a repetiçao desaticando a
     * view que coleta essa informação
     */
    private fun despesaNaoSeRepete() {

        binding.tilDataLimiteRepetir.isEnabled = false
        binding.ivRecorrente.isClickable = false

        binding.dataLimiteRepetir.setText("")

        binding.edtRepetir.setText(getString(R.string.Nao_repetir))
        binding.edtRepetir.clearFocus()
    }

    /**
     * Permite que o uaurio selecione a data limite da repeticao desbloqueando a view que coleta essa
     * informação e dando foco nela, além de atualizar a interface com dados da repeticao
     */
    private fun despesaSeRepete(qtdRepeticoes: Int, tipoIntervalo: String) {

        binding.tilDataLimiteRepetir.isEnabled = true
        binding.ivRecorrente.isClickable = true
        binding.dataLimiteRepetir.requestFocus()

        binding.edtRepetir.setText(String.format(getString(R.string.Repetir_a_cada_x_y), qtdRepeticoes.toString(), tipoIntervalo))
    }

    /**
     * Mostra um BottomSheet para coletar os dados de repeticao da despesa, nele a entrada é
     * coletada e verificada, se tudo estiver certo os valores sao passados via callback, senao
     * o usuario é notificado para corrigir o que for necessario
     */
    private fun mostrarBottomSheetRepetir(callback: (Int, String, Recorrencia.Tipo?) -> Any) {
        BottomSheetRepetir(callback, this@FragAddDespesa, viewModel.qtdRepeticoes
            ?: 1, viewModel.tipoRecorrencia).mostrar()
    }

    private fun initCampoData() {
        binding.ivDataPicker.setOnClickListener(object : AnimatedClickListener() {
            override fun onClick(view: View) {
                super.onClick(view)

                val dataInicial = viewModel.dataDePagamentoDaDespesa ?: agoraEmMillis()

                mostrarDataPicker(dataInicial) { _: Long, dataFormatada: String ->
                    binding.dataPagamento.setText(dataFormatada)
                    binding.dataPagamento.setSelection(dataFormatada.length)
                }
            }
        })
        binding.dataPagamento.addTextChangedListener(MascaraData.mascaraData())
        binding.dataPagamento.addTextChangedListener {
            // o valor setado será null até que seja digitada uma data valida
            viewModel.dataDePagamentoDaDespesa = dataFormatadaParaLong(it.toString(), DD_MM_AAAA)
        }

    }

    private fun mostrarDataPicker(dataInicial: Long, callback: (Long, String) -> Unit) {

        val picker = MaterialDatePicker.Builder.datePicker().setSelection(dataInicial).setTitleText("").build()

        // tem um bug no picker que retorna a data com um dia a menos
        picker.addOnPositiveButtonClickListener {

            val dataCorreta = corrigirFusoHorario(it)
            val dataFormatada = formatarData(dataCorreta, DD_MM_AAAA)
            callback.invoke(dataCorreta, dataFormatada)
        }


        picker.show(parentFragmentManager, "tag");
    }

    private fun initCampoDeNome() {
        binding.tilNome.counterMaxLength = Despesa.COMPRIMENTO_MAXIMO_NOME
        binding.nome.filters = arrayOf(InputFilter.LengthFilter(Despesa.COMPRIMENTO_MAXIMO_NOME))
    }

    private fun initCampoObservacoes() {
        binding.tilObservacoes.counterMaxLength = Despesa.COMPRIMENTO_MAXIMO_OBSERVACOES
        binding.observacoes.filters = arrayOf(InputFilter.LengthFilter(Despesa.COMPRIMENTO_MAXIMO_OBSERVACOES))
    }

    private fun initTextViewValoreMoeda() {
        binding.tvValor.text = "999,95"
        binding.tvMoeda.text = Currency.getInstance(Locale.getDefault()).symbol
        binding.tvValor.setOnClickListener {
            TecladoCalculadora.Builder()
                .valorInicial(binding.tvValor.text.toString().emDouble())
                .callback { valor: String ->

                    binding.tvValor.text = valor.emMoedaSemSimbolo()
                    viewModel.valorDespesa = valor
                }.titulo(getString(R.string.Digite_o_valor_da_despesa))
                .build().show(parentFragmentManager, "")

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



