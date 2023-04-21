package gmarques.debtv4.presenter.pop_ups

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.util.Range
import android.view.*
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.DialogFragment
import com.google.android.material.snackbar.Snackbar
import gmarques.debtv4.R
import gmarques.debtv4.databinding.LayoutTecladoCalculadoraBinding
import gmarques.debtv4.presenter.outros.AnimatedClickListener
import gmarques.debtv4.presenter.outros.UIUtils
import java.math.MathContext
import java.math.RoundingMode


/**
 * @Author: Gilian Marques
 * @Date: quinta-feira, 30 de março de 2023 às 20:23
 */
class TecladoCalculadora : DialogFragment() {

    companion object {
        const val OP_SOMA = "+"
        const val OP_SUBT = "-"
        const val OP_MULT = "x"
        const val OP_DIV = "÷"
        const val OP_PORCE = "%"
        const val REGEX_OPERADORES = "+x÷%-"
        const val REGEX_OPERADORES_SEM_SUBT = "+x÷%"

    }

    private var titulo: String = ""
    private var mostrandoErrosNaFormula: Boolean = false
    private var colorAccent: Int = 0
    private var valorInicial: String = ""
    private lateinit var callback: (String) -> Unit
    private lateinit var binding: LayoutTecladoCalculadoraBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = LayoutTecladoCalculadoraBinding.inflate(layoutInflater)
        aplicarFlags()
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
            it?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND) // remove o dimmer
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
        colorAccent = UIUtils.corAttr(android.R.attr.colorAccent, requireActivity())
        initBotoesNumeros()
        initBotoesOperadores()
        initEdtValor()
        initBotaoIgual()
        initBotaoPonto()
        initBotaoApagar()
        initBotaoConcluir()
        initToolbar()
    }

    private fun initToolbar() {
        binding.layoutToolbar.titulo.text = titulo
        binding.layoutToolbar.voltar.setOnClickListener {
            dismiss()
        }


    }

    private fun initBotaoConcluir() {
        binding.tvConcluir.setOnClickListener(object : AnimatedClickListener() {
            override fun onClick(view: View) {
                super.onClick(view)

                val formula = binding.edtValor.text.toString()

                if (resultadoValido(formula)) {
                    callback.invoke(formula)
                    dismiss()
                } else {
                    notificarErro(getString(R.string.Resultado_deve_ser_um_numero_positivo_com))
                }
            }
        })
    }

    /**
     * retorna true se o resultado recebido for valido, veja os testes
     * para entender melhor o que é um resultado valido
     */
    @VisibleForTesting
    fun resultadoValido(resultado: String): Boolean {
        val resultadoValido = Regex("""[\d]+[.]?[\d]?[\d]?""")
        return resultadoValido.matches(resultado)
    }

    private fun initBotaoApagar() {
        binding.tvApagar.setOnClickListener(object : AnimatedClickListener() {
            override fun onClick(view: View) {
                super.onClick(view)

                val formula = binding.edtValor.text.toString()
                val comecoSelecao = if (binding.edtValor.selectionStart == -1) formula.length - 1 else binding.edtValor.selectionStart
                val finalSelecao = if (binding.edtValor.selectionEnd == -1) formula.length - 1 else binding.edtValor.selectionEnd

                if (comecoSelecao != finalSelecao) {
                    binding.edtValor.setText(formula.removeRange(comecoSelecao until finalSelecao))
                    binding.edtValor.setSelection(binding.edtValor.text.length - 1)
                } else {
                    binding.edtValor.setText(formula.removeRange((comecoSelecao - 1).coerceAtLeast(0), comecoSelecao))
                    binding.edtValor.setSelection((comecoSelecao - 1).coerceAtLeast(0))
                }
            }
        })
        binding.tvApagar.setOnLongClickListener {
            binding.edtValor.setText("")
            UIUtils.vibrar(UIUtils.Vibracao.INTERACAO)
            true
        }
    }

    private fun initBotaoIgual() {
        binding.tvIgual.setOnClickListener(object : AnimatedClickListener() {
            override fun onClick(view: View) {
                super.onClick(view)

                var formula = binding.edtValor.text.toString()
                formula = incluirZerosOndeNecessario(formula)
                val erros = notificarSeHouveremErrosNaFormula(formula)
                if (!erros) mostrarResultado(formula)
            }
        })
    }

    private fun initBotaoPonto() {
        binding.tvPonto.setOnClickListener(object : AnimatedClickListener() {
            override fun onClick(view: View) {
                super.onClick(view)
                val ponto = (view as TextView).text.toString()
                incluirCaractere(ponto)
            }
        })
    }

    private fun initEdtValor() {
        binding.edtValor.showSoftInputOnFocus = false
        binding.edtValor.setText(valorInicial)
        binding.edtValor.requestFocus()

    }

    private fun initBotoesNumeros() {

        val numeros = arrayOf(binding.tv1, binding.tv2, binding.tv3, binding.tv4, binding.tv5, binding.tv6, binding.tv7, binding.tv8, binding.tv9, binding.tv0)

        numeros.forEach {
            it.setOnClickListener(object : AnimatedClickListener() {
                override fun onClick(view: View) {
                    super.onClick(view)

                    val numero = (view as TextView).text.toString()
                    incluirCaractere(numero)

                }
            })
        }

    }

    private fun initBotoesOperadores() {

        val views = arrayOf(binding.tvSoma, binding.tvSubtr, binding.tvMult, binding.tvDiv, binding.tvPorcento)

        views.forEach {
            it.setOnClickListener(object : AnimatedClickListener() {
                override fun onClick(view: View) {
                    super.onClick(view)

                    val operador = (view as TextView).text.toString()
                    val formula = binding.edtValor.text.toString()

                    // pode add apenas o sinal '-' no começo da formula
                    if (formula.isEmpty() && operador != OP_SUBT) return
                    else if (formula.isNotEmpty() && REGEX_OPERADORES.contains(formula)) return

                    // simula um aperto no botao de = do teclado para calcular a formula existente antes de adicionar ourto operador
                    // mas apenas se o cursos estiver no final da formula, se nao for o caso pode ser que o usuario esteja apenas
                    //querendo substituir o operador atual, e por tanto o calculo nao deve ser realizado
                    if (valorNaTelaEhUmaFormulaValida(formula) && binding.edtValor.selectionStart == formula.length) binding.tvIgual.callOnClick()

                    incluirCaractere(operador)

                }
            })
        }
    }

    @SuppressLint("SetTextI18n")
    private fun mostrarResultado(formula: String) {

        try {

            Log.d("USUK", "TecladoCalculadora.mostrarResultado: $formula")
            val resultado = Calculadora().calcular(formula)
            Log.d("USUK", "TecladoCalculadora.mostrarResultado: $resultado")
            binding.edtValor.setText(resultado)
            binding.edtValor.setSelection(resultado.length)

            binding.tvHistorico.text = "${binding.tvHistorico.text}\n$formula = $resultado"
            binding.scroll.scrollTo(0, binding.tvHistorico.height);

            UIUtils.vibrar(UIUtils.Vibracao.SUCESSO)

        } catch (e: java.lang.Exception) {

            notificarErro(getString(R.string.Formula_invalida))

            val erroSpan = SpannableString(formula).apply { setSpan(ForegroundColorSpan(colorAccent), 0, formula.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE) }
            binding.edtValor.setText(erroSpan)

            UIUtils.vibrar(UIUtils.Vibracao.ERRO)
            binding.edtValor.setSelection(formula.length)

            Log.e("USUK", "Calculadora.calcular: $formula")
            e.printStackTrace()
        }
    }

    private fun notificarErro(string: String) {
        Snackbar.make(requireContext(), binding.root, string, Snackbar.LENGTH_LONG).show()
        UIUtils.vibrar(UIUtils.Vibracao.ERRO)
        binding.edtValor.requestFocus()
    }

    /**
     * Por comodidade, usuario pode digitar formulas sem o numero antes do ponto, ex: 15.99+.96
     * nesse caso o app insere 0 um na formula ficando 15.99+0.96
     */
    @VisibleForTesting
    fun incluirZerosOndeNecessario(formula: String): String {

        var novaFormula = formula

        val regexPrimeiroNumeroFormula = Regex("""^[.][\d]+""")
        val regexSegundoNumeroFormula = Regex("""[$REGEX_OPERADORES]([.][\d]+)""")

        val matchPrim = regexPrimeiroNumeroFormula.find(novaFormula, 0)
        if (matchPrim != null) novaFormula = novaFormula.replace(matchPrim.value, "0${matchPrim.value}")


        val matchSeg = regexSegundoNumeroFormula.find(novaFormula, 0)
        if (matchSeg != null) novaFormula = novaFormula.replace(matchSeg.groups[1]!!.value, "0${matchSeg.groups[1]!!.value}")

        return novaFormula
    }

    /**
     * Se [encontrarErrosNaFormula] encontrar erros, essa função dispara um fluxo para mostrar
     * um snackbar e vibrar o aparelho
     * @return false se nao houverem erros
     */
    private fun notificarSeHouveremErrosNaFormula(formula: String): Boolean {
        return if (encontrarErrosNaFormula(formula)) {
            notificarErro(getString(R.string.Formula_invalida))
            true
        } else false
    }

    /**
     * mostra no campo de valor a posiçao dos caracteres invalidos atraves de Spannables
     *
     */
    private fun mostrarErrosNaUi(indicesDosErros: ArrayList<IntRange>, formula: String) {
        mostrandoErrosNaFormula = true

        val spannableStr = SpannableString(formula)

        indicesDosErros.forEach {
            spannableStr.setSpan(ForegroundColorSpan(colorAccent), it.first, (it.last + 1).coerceAtMost(formula.length), Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        }

        binding.edtValor.setText(spannableStr)
        binding.edtValor.setSelection(spannableStr.toString().length)
        mostrandoErrosNaFormula = false
    }

    private fun valorNaTelaEhUmaFormulaValida(formula: String): Boolean {
        return Calculadora.regexFormulaValida.matches(formula)
    }

    /**
     * Usa regex para encontrar padroes comuns de erro
     * @return false se nao houverem erros
     */
    private fun encontrarErrosNaFormula(formula: String): Boolean {

        val areaErros = ArrayList<IntRange>()

        val pontoSeguidaDeOperador = Regex("""([.])[$REGEX_OPERADORES]""")
        val operadoresSeguidosDePonto = Regex("""[$REGEX_OPERADORES]([.])""")
        val pontoNoComeco = Regex("""^([.])""")
        val pontoNoFim = Regex("""([.])$""")
        val opNoComeco = Regex("""^([$REGEX_OPERADORES_SEM_SUBT])""")// o sinal - pode ficar no começo da formula
        val opNoFim = Regex("""([$REGEX_OPERADORES])$""")

        val padroes = arrayListOf(pontoSeguidaDeOperador, operadoresSeguidosDePonto, pontoNoComeco, pontoNoFim, opNoComeco, opNoFim)

        padroes.forEach { padrao -> padrao.findAll(formula).forEach { areaErros.add(it.groups[1]!!.range) } }

        return if (areaErros.isEmpty()) false
        else {
            mostrarErrosNaUi(areaErros, formula)
            true
        }
    }

    /**
     * Essa função atualiza a interface com a formula corrigida em caso de operadores duplicados
     */
    private fun corrigirOperadoresIlegais() {
        val selecao = binding.edtValor.selectionStart

        val formula = binding.edtValor.text.toString()

        val formulaCorrigida = removerOperadoresEmSequenciaDaFormula(formula)

        binding.edtValor.setText(formulaCorrigida)

        val selecaoSegura = if (Range(0, formulaCorrigida.length).contains(selecao)) selecao else formulaCorrigida.length
        binding.edtValor.setSelection(selecaoSegura)
    }

    /**
     * Essa função atualiza a interface com a formula corrigida em caso de pontos duplicados
     */
    private fun corrigirPontosIlegais() {

        val selecao = binding.edtValor.selectionStart

        val formula = binding.edtValor.text.toString()
        val formulaCorrigida = removerPontosMultiplos(formula)

        binding.edtValor.setText(formulaCorrigida)
        binding.edtValor.setSelection(formulaCorrigida.length)

        val selecaoSegura = if (Range(0, formulaCorrigida.length).contains(selecao)) selecao else formulaCorrigida.length
        binding.edtValor.setSelection(selecaoSegura)
    }

    private fun corrigirCasasDecimaisIlegais() {

        val selecao = binding.edtValor.selectionStart

        val formula = binding.edtValor.text.toString()
        val formulaCorrigida = removerCasasDecimaisIlegais(formula)

        binding.edtValor.setText(formulaCorrigida)
        binding.edtValor.setSelection(formulaCorrigida.length)

        val selecaoSegura = if (Range(0, formulaCorrigida.length).contains(selecao)) selecao else formulaCorrigida.length
        binding.edtValor.setSelection(selecaoSegura)

    }

    /**
     * Busca por operadores em sequencia na formula e os remove, mantendo o ultimo operador
     * da sequencia removida. Essa função leva em conta os numeros negativos, os mantendo na formula.
     *
     * Essa funcao busca corrigir erros simples que podem ocorrer durante a digitação da formula
     * e nao todos os possiveis erros.
     */
    @VisibleForTesting
    fun removerOperadoresEmSequenciaDaFormula(formula: String): String {
        if (formula.length <= 1) return formula

        /**
         * Captura um operador seguido por outros operadores, assim um operador sozinho nao é
         * enquadrado no padrao, o padrao nao considera operadores seguidos pelo sinal '-' (subraçao) pois
         * este inbdica que o numero a seguir é negativo
         */
        val regexOpLal = Regex("""[$REGEX_OPERADORES][$REGEX_OPERADORES_SEM_SUBT]+""")
        var novaFormula = formula

        while (regexOpLal.containsMatchIn(novaFormula)) {
            val match = regexOpLal.find(novaFormula, 0) ?: break
            val substituicao = match.value.last()
            novaFormula = novaFormula.replace(match.value, substituicao.toString())
        }
        return novaFormula
    }

    /**
     * Essa funcao busca corrigir erros simples que podem ocorrer durante a digitação da formula
     * e nao todos os possiveis erros.
     * @return uma versao da formula onde os numeros nao terao mais do que um ponto
     */
    @VisibleForTesting
    fun removerPontosMultiplos(formula: String): String {

        if (formula.length <= 1) return formula

        val regexOperadores = Regex("""[$REGEX_OPERADORES]""")
        val regexPontos = Regex("""[.]+""")
        val numeros = regexOperadores.split(formula)

        var novaFormula = formula

        for (numero in numeros) {
            if (!numero.contains(".")) continue

            val posUltimaPonto = regexPontos.findAll(numero).last().range.last

            var novoNumero = ""
            for (i in numero.indices) {
                val charNum = numero[i].toString()
                if (charNum != "." || i == posUltimaPonto) novoNumero += charNum
            }

            novaFormula = novaFormula.replace(numero, novoNumero)

        }
        return novaFormula
    }

    /**
     * Encontra e limita casas decimais a no maximo dois numeros
     */
    @VisibleForTesting
    fun removerCasasDecimaisIlegais(formula: String): String {

        val regex = Regex("""([.][\d]{2})[\d]+""")
        var novaFormula = formula

        while (regex.containsMatchIn(novaFormula)) {
            val match = regex.find(novaFormula, 0)
            novaFormula = novaFormula.replace(match!!.value, match.groups[1]!!.value)
        }
        return novaFormula

    }

    @SuppressLint("SetTextI18n")
    private fun incluirCaractere(valor: String) {

        val textoAtual = binding.edtValor.text
        val localDeInsercaoInicio = binding.edtValor.selectionStart
        val localDeInsercaoFinal = binding.edtValor.selectionEnd


        if (localDeInsercaoInicio == -1) {
            //sem selecao
            binding.edtValor.setText("$textoAtual$valor")
        } else if (localDeInsercaoFinal > localDeInsercaoInicio) {
            //parte do texto foi selecionado
            val novoTexto = textoAtual.delete(localDeInsercaoInicio, localDeInsercaoFinal).insert(localDeInsercaoInicio, valor)
            binding.edtValor.text = novoTexto
            binding.edtValor.setSelection(localDeInsercaoInicio + 1)
        } else {
            // o cursor esta na view mas sem selecionar texto
            val novoTexto = textoAtual.insert(localDeInsercaoInicio, valor)
            binding.edtValor.text = novoTexto
            binding.edtValor.setSelection(localDeInsercaoInicio + 1)
        }

        corrigirOperadoresIlegais()
        corrigirPontosIlegais()
        corrigirCasasDecimaisIlegais()
    }

    class Builder {

        private val calc = TecladoCalculadora()

        fun callback(callback: (String) -> Unit) = apply {
            calc.callback = callback
        }

        fun valorInicial(valorInicial: String) = apply {
            calc.valorInicial = valorInicial
        }

        fun titulo(titulo: String) = apply {
            calc.titulo = titulo
        }

        fun build() = calc
    }

    class Calculadora {
        companion object {
            /**
             * Esse regex encontra um numero que pode ou nao ser negativo e agrupa o numero e seu sinal
             */
            private const val GRUPO_DIGITOS_PRE_OP = """[$REGEX_OPERADORES]?([-]?[\d.]+)"""

            /**
             * Busca o operador que define qual operaçao sera feita com os valores extraidos da formula,
             */
            private const val REGEX_OPERACAO = """([\d]?[$REGEX_OPERADORES])"""

            /**
             * Mesmo que [Calculadora.GRUPO_DIGITOS_PRE_OP] mas um pouco diferente ja que é usado para achar o numero
             * depois do operador da formula
             */
            private const val GRUPO_DIGITOS_POS_OP = """([$OP_SUBT]?[\d.]+)"""

            /**
             * Captura um numero que pode ou nao ser negativo, seguido por um operador qualquer e outro
             * numero que pode ou nao ser negativo
             *
             * Obs: para capturar um numero negativo no começo da formula deve-se adcionar um operador +
             * antes dele. veja o setter de [novaFormula] para entender melhor.
             */
            val regexFormulaValida = Regex("""$GRUPO_DIGITOS_PRE_OP$REGEX_OPERACAO$GRUPO_DIGITOS_POS_OP""")

        }

        private val escala = 2
        private val arredondamento = RoundingMode.HALF_UP
        private val mathContext = MathContext(12, arredondamento)


        private var novaFormula = ""
            /**
             *
             * Esse setter adiciona uma sinal de soma a uma formula que começe com
             * um numero negativo para que os padroes regex consigam identificar que o
             * sinal de menos significa que o primeiro numero da formula é negativo.
             *
             * Os padroes regex atuais só entendem que um numero é negativo se seu sinal de menos
             * for precedido por outro operador qualquer (+,x,÷,%,-) neste exemplo 25*-35,
             * entende-se que 35 é negativo ja que o sinal de menos é precedido pelo sinal de multiplicaçao
             * que representa a operaçao a ser feita agora, se o numero da frente é negativo os
             * padroes regex nao conseguem identificar ex: -25*-35, aqui apenas o 35 é reconhecido
             * como negativo.
             */
            set(value) {
                field = if (value.startsWith(OP_SUBT)) "$OP_SOMA$value"
                else value
            }

        /**
         * Trata a formula, manda calcular e trata o resultado, retornando-o para o cliente
         */
        fun calcular(formula: String): String {

            novaFormula = formula

            val resultado = extraireCalcularValores()

            /* Se o resultado da formula for negativo, um sinal de + sera adicionado na frente do resultado
             * (veja o setter da variavel novaFormula), esse sinal de soma deve ser removido ao fim dos calculos*/
            return if (resultado.startsWith("$OP_SOMA$OP_SUBT")) resultado.drop(1) else resultado
        }

        /**
         *  Extrai os valores da formula e seu operador, chamando a função adequada para realizar
         *  o calculo dos valores
         *  @throws Exception se a formula for invalida
         *  @throws NumberFormatException se houver erro numerico no momento dos calculos
         */
        private fun extraireCalcularValores(): String {
            val match = regexFormulaValida.find(novaFormula, 0)
            if (match == null || match.groups.count() != 4) throw java.lang.Exception("Formula invalida Formula: '$novaFormula'")

            val valor1 = match.groups[1]!!.value
            val operador = match.groups[2]!!.value
            val valor2 = match.groups[3]!!.value

            return when (operador) {
                OP_MULT  -> multiplicar(valor1, valor2)
                OP_DIV   -> dividir(valor1, valor2)
                OP_SOMA  -> somar(valor1, valor2)
                OP_SUBT  -> subtrair(valor1, valor2)
                OP_PORCE -> porcentagem(valor1, valor2)
                else     -> throw java.lang.Exception("operador desconhecido operador: '$operador', formula: '$novaFormula'")
            }.toPlainString()


        }

        /**
         *  @throws NumberFormatException se houver erro numerico no momento dos calculos
         */
        private fun porcentagem(valor1: String, valor2: String) =
            valor1.toBigDecimal().multiply(valor2.toBigDecimal()).divide("100".toBigDecimal(), mathContext).setScale(escala, arredondamento)

        /**
         *  @throws NumberFormatException se houver erro numerico no momento dos calculos
         */
        private fun subtrair(valor1: String, valor2: String) =
            valor1.toBigDecimal().subtract(valor2.toBigDecimal(), mathContext).setScale(escala, arredondamento)

        /**
         *  @throws NumberFormatException se houver erro numerico no momento dos calculos
         */
        private fun somar(valor1: String, valor2: String) =
            valor1.toBigDecimal().add(valor2.toBigDecimal(), mathContext).setScale(escala, arredondamento)

        /**
         *  @throws NumberFormatException se houver erro numerico no momento dos calculos
         */
        private fun dividir(valor1: String, valor2: String) =
            valor1.toBigDecimal().divide(valor2.toBigDecimal(), mathContext).setScale(escala, arredondamento)

        /**
         *  @throws NumberFormatException se houver erro numerico no momento dos calculos
         */
        private fun multiplicar(valor1: String, valor2: String) =
            valor1.toBigDecimal().multiply(valor2.toBigDecimal(), mathContext).setScale(escala, arredondamento)


    }

}
