package gmarques.debtv4.presenter

import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.addCallback
import androidx.core.animation.doOnEnd
import androidx.core.view.animation.PathInterpolatorCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import gmarques.debtv4.databinding.LayoutBottomSheetBinding
import gmarques.debtv4.presenter.outros.UIUtils

/**
 * @Author: Gilian Marques
 * @Date: quinta-feira, 30 de março de 2023 às 20:23
 */
class BetterBottomSheet : DialogFragment() {
    private var dismissListener: (() -> Unit)? = null
    private val duracao = 250L
    private var customView: View? = null
    private lateinit var binding: LayoutBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = LayoutBottomSheetBinding.inflate(layoutInflater)
        binding.root.visibility = View.INVISIBLE
        aplicarFlags()
        customView?.let {
            binding.container.addView(customView)
        }
        return binding.root
    }

    /**
     * Aplica as flags necessarias para garantir que este dialogo sera exibido em tela cheia
     * , sem fundo e sem o dimmer padrao alem de corrigir a cor do navbar
     */
    private fun aplicarFlags() {
        dialog?.window.let {
            it?.requestFeature(Window.FEATURE_NO_TITLE)
            it?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            it?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND); // remove o dimmer
            it?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)// deixa a navbar coma cor certa
        }
    }

    /**
     * Aplica os parametros necessarias para garantir que este dialogo sera exibido em tela cheia
     */
    override fun onStart() {
        super.onStart()
        dialog ?: return
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.MATCH_PARENT
        dialog?.window?.setLayout(width, height)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prepararViewsParaAnimar()
        initParentView()
        configurarFechamento()
        permitirInputsDoUsuario()

        //essa verificação serve para impedir erro de view nula qdo se reinicia a activity com o Bsheet na tela (pelo android studio)
        if (customView == null) {
            Log.e("USUK", "BetterBottomSheet.onViewCreated: Esse BottomSheet precisa de uma view para ser exibido")
            dismiss()
        }
    }

    private fun permitirInputsDoUsuario() {
        //TODO("Not yet implemented")
    }

    /**
     * Essa função tira do sistema a capacidade de fechar o dialogo  e cria um listener pra permitir
     * que o mesmo só sera fechado por si proprio, quando o usuario clicar em voltar ou fora da area
     * de interaçao do bottomsheet. Isso serve para garantir que a animnaçao de saida sera executada
     * antes do fechamento do dialogo
     */
    private fun configurarFechamento() {
        // garanto que o dialogo nao possa ser fechado pelo sistema, isso nao afeta o bottomsheet em si
        // apenas o dialogo que ta hospedando ele
        dialog?.setCancelable(false)

        // aqui defino um listener pro botao de voltar e verifico se o bottomsheet é cancelavel antes de fechar
        (dialog as androidx.activity.ComponentDialog).onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (isCancelable) dismiss()
        }

    }

    /**
     * Define o evento de clique que fecha o bs (caso ele seja cancelavel) quando o usuario clica
     * fora da area de interaçao
     */
    private fun initParentView() {
        binding.parent.setOnClickListener { if (isCancelable) dismiss() }
    }

    /**
     * este override serve para fazer o dialogo esperar a animaçao de fechar antes de sair da tela
     */
    override fun dismiss() {
        animarSaida()
    }

    /**
     * Espera ate que a view do bottomsheet e a customview estejam prontas e inicia a animaçao de entrada
     */
    private fun prepararViewsParaAnimar() {

        binding.root.post {
            customView?.post {
                animarEntrada()
                binding.root.visibility = View.VISIBLE

            }
        }
    }

    private fun animarEntrada() {
        val interpolator = PathInterpolatorCompat.create(0.250f, 0.460f, 0.450f, 0.940f)
        val alturaTela = UIUtils.tamanhoTelaSemStatusOuNavBar(requireActivity()).second
        val alturaContainer = binding.container.height
        val containerAnim = ValueAnimator
            .ofFloat(alturaTela.toFloat(), alturaTela.toFloat() - alturaContainer.toFloat())
        containerAnim.duration = duracao
        containerAnim.interpolator = interpolator
        containerAnim.addUpdateListener {
            binding.container.y = (it.animatedValue as Float)
        }
        containerAnim.start()


    }

    private fun animarSaida() {

        val interpolator = PathInterpolatorCompat.create(0.250f, 0.460f, 0.450f, 0.940f)
        val bottom = binding.parent.height
        val height = binding.container.y

        val animator = ValueAnimator.ofFloat(height, bottom.toFloat())
        animator.addUpdateListener {
            binding.container.y = (it.animatedValue as Float)
        }
        animator.interpolator = interpolator
        animator.doOnEnd {
            super.dismiss()
            dismissListener?.invoke()
        }
        animator.start()
    }

    fun customView(view: View) = apply { this.customView = view }

    fun show(parentFragmentManager: FragmentManager) = apply {
        show(parentFragmentManager, "$this")
    }

    fun cancelavel(b: Boolean) = apply {
        isCancelable = b
    }

    fun onDismiss(function: () -> Unit) = apply {
        dismissListener = function
    }
}
