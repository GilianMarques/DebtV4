package gmarques.debtv4.presenter.add_despesa

import android.animation.ValueAnimator
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.view.animation.PathInterpolatorCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.MaterialDatePicker
import gmarques.debtv4.R
import gmarques.debtv4.databinding.FragAddDespesaBinding
import gmarques.debtv4.databinding.LayoutTipoDespesaBinding
import gmarques.debtv4.domain.entidades.Despesa
import gmarques.debtv4.domain.extension_functions.ExtFunctions.Companion.porcentoDe
import gmarques.debtv4.presenter.BetterBottomSheet
import gmarques.debtv4.presenter.main.CustomFrag
import gmarques.debtv4.presenter.outros.AnimatedClickListener
import gmarques.debtv4.presenter.outros.Mascara
import gmarques.debtv4.presenter.outros.UIUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.*
import java.util.*
import kotlin.math.abs

class FragAddDespesa : CustomFrag() {


    private lateinit var viewModel: FragAddDespesaViewModel
    private lateinit var binding: FragAddDespesaBinding
    private val animsAtualizadasPeloAppBar = ArrayList<ValueAnimator>()

    private val mascaraDeData = "dd/MM/yyyy"

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
        initToolbar(binding, getString(R.string.Nova_despesa))
        initAnimacaoDosCantosDoScrollView()
        initAnimacaoDeCorDaStatusBar()
        initTextViewValoreMoeda()
        initCampoDeNome()
        initCampoData()
        initCampoTipoDespesa()
        initCampoObservacoes()
    }

    @Suppress("UsePropertyAccessSyntax")
    private fun initCampoTipoDespesa() {
        val listener = { view: View, b: Boolean ->
            if (b) {
                lifecycleScope.launch {
                    UIUtils.ocultarTeclado(view)
                    mostrarBsTipoDespesa()
                }
            }
        }
        binding.tilTipo.setOnFocusChangeListener(listener)
        binding.tipoDespesa.setOnFocusChangeListener(listener)
    }

    private fun mostrarBsTipoDespesa() {

        val ui = LayoutTipoDespesaBinding.inflate(layoutInflater)

        val bsheet = BetterBottomSheet()
            .customView(ui.root)
            .cancelavel(true)
            .onDismiss { binding.tipoDespesa.clearFocus() }
            .show(parentFragmentManager)


        val acao = { texto: String ->
            binding.tipoDespesa.setText(texto)
            bsheet.dismiss()
            binding.dataPgto.requestFocus()
        }

        ui.recorrente.setOnClickListener { acao.invoke(ui.recorrente.text.toString()) }
        ui.cartao.setOnClickListener { acao.invoke(ui.cartao.text.toString()) }
        ui.poupanca.setOnClickListener { acao.invoke(ui.poupanca.text.toString()) }

    }

    private fun initCampoData() {
        binding.ivDataPicker.setOnClickListener(object : AnimatedClickListener() {
            override fun onClick(view: View) {
                super.onClick(view)
                mostrarDataPicker()
            }
        })
        binding.dataPgto.addTextChangedListener(Mascara.mascaraData())
    }

    private fun mostrarDataPicker() {

        val picker = MaterialDatePicker.Builder.datePicker().setSelection(coletarDataInicial())
            .setTitleText("").build()

        // tem um bug no picker que retorna a data com um dia a menos
        picker.addOnPositiveButtonClickListener {

            val dataCorreta =
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
                        .plusDays(1).toInstant(OffsetDateTime.now().offset).toEpochMilli()

            val dataFormat = SimpleDateFormat(mascaraDeData, Locale.getDefault())
            val dataFormatada = dataFormat.format(dataCorreta)
            binding.dataPgto.setText(dataFormatada)
            binding.dataPgto.setSelection(dataFormatada.length)
        }


        picker.show(parentFragmentManager, "tag");
    }

    /**
     * Se o usuario ja selecionou uma data pelo Picker ou digitou um valor no campo de data e esse
     * valor for valido, ele sera convertido em long e retornado para ser usado no picker, senao,
     * um long representando a data de hoje sera retornada
     */
    private fun coletarDataInicial() = try {
        val format = SimpleDateFormat(mascaraDeData, Locale.getDefault())
        format.parse(binding.dataPgto.text.toString())!!.time
    } catch (e: java.lang.Exception) {
        MaterialDatePicker.todayInUtcMilliseconds()
    }

    private fun initCampoDeNome() {
        binding.tilNome.counterMaxLength = Despesa.COMPRIMENTO_MAXIMO_NOME
        binding.nome.filters = arrayOf(InputFilter.LengthFilter(Despesa.COMPRIMENTO_MAXIMO_NOME))
    }

    private fun initCampoObservacoes() {
        binding.tilObservacoes.counterMaxLength = Despesa.COMPRIMENTO_MAXIMO_OBSERVACOES
        binding.observacoes.filters =
                arrayOf(InputFilter.LengthFilter(Despesa.COMPRIMENTO_MAXIMO_OBSERVACOES))
    }

    private fun initTextViewValoreMoeda() {
        binding.tvValor.text = "999,95"
        binding.tvMoeda.text = Currency.getInstance(Locale.getDefault()).symbol

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

        val statusBarColorAnimatior =
                ValueAnimator.ofArgb(requireActivity().window.statusBarColor, targetColor)
        statusBarColorAnimatior.interpolator =
                PathInterpolatorCompat.create(1.000f, 0.000f, 1.000f, 1.030f)
        statusBarColorAnimatior.addUpdateListener { animation ->
            requireActivity().window.statusBarColor = animation.animatedValue as Int

            if (animation.animatedFraction < 0.80) {
                decorView.systemUiVisibility = decorViewFlags
            } else {
                decorView.systemUiVisibility =
                        decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }

        animsAtualizadasPeloAppBar.add(statusBarColorAnimatior)
    }

    private fun initAnimacaoDosCantosDoScrollView() = binding.nestedScroll.post {

        val drawable = binding.nestedScroll.background as GradientDrawable

        // pego o raio_X do canto superior esquerdo e uso como parametro para os raios da parte superior do drawable
        val raioEmDp = drawable.cornerRadii?.get(1) ?: 0f

        val cornerAnimation = ValueAnimator.ofFloat(raioEmDp, 0f)
        cornerAnimation.interpolator = PathInterpolatorCompat.create(1.000f, 0.000f, 1.000f, 1.030f)
        cornerAnimation.addUpdateListener { animation ->
            val cornerRadius = animation.animatedValue as Float
            drawable.cornerRadii = floatArrayOf(
                cornerRadius, cornerRadius, //top left
                cornerRadius, cornerRadius, // top right
                0f, 0f, // bottom right
                0f, 0f // bottom left
            )
        }

        animsAtualizadasPeloAppBar.add(cornerAnimation)
    }


}