package gmarques.debtv4.presenter

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.fragment.app.DialogFragment
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


    // TODO: remover foco da view dps de calcular

    private var colorAccent: Int = 0
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
        initBotaoVirgula()
        initBotaoApagar()
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

                val formula = binding.edtValor.text.toString()
                val erros = mostrarErrosNaFormula(formula)
                if (!erros) mostrarResultado(formula)
            }
        })
    }

    private fun initBotaoVirgula() {
        binding.tvVirgula.setOnClickListener(object : AnimatedClickListener() {
            override fun onClick(view: View) {
                super.onClick(view)
                val virgula = (view as TextView).text.toString()
                incluirCaractere(virgula)
            }
        })
    }

    private fun initEdtValor() {
        binding.edtValor.showSoftInputOnFocus = false
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

                    // pode add apenas o sinal '-' no começo da formula
                    if (binding.edtValor.text.isEmpty() && operador != OP_SUBT) return
                    else if (binding.edtValor.text.isNotEmpty() && REGEX_OPERADORES.contains(binding.edtValor.text.toString())) return

                    incluirCaractere(operador)

                }
            })
        }
    }

    @SuppressLint("SetTextI18n")
    private fun mostrarResultado(formula: String) {

        try {

            Log.d("USUK", "TecladoCalculadora.mostrarResultado: $formula")
            val resultado = Calculadora(formula).calcular()
            binding.edtValor.setText(resultado)
            binding.edtValor.setSelection(resultado.length)

            binding.tvHistorico.text = "${binding.tvHistorico.text}\n$formula"

            UIUtils.vibrar(UIUtils.Vibracao.SUCESSO)

        } catch (e: java.lang.Exception) {

            val erroSpan = SpannableString(formula)
                .apply { setSpan(ForegroundColorSpan(colorAccent), 0, formula.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE) }

            binding.edtValor.setText(erroSpan)
            UIUtils.vibrar(UIUtils.Vibracao.ERRO)

            binding.edtValor.setSelection(formula.length)


            Log.e("USUK", "Calculadora.calcular: $formula")
            e.printStackTrace()
        }
    }

    /**
     * Usa regex para encontrar padroes comuns de erro e os marca na tela para o usuario ver
     */
    private fun mostrarErrosNaFormula(formula: String): Boolean {
        val areaErros = ArrayList<IntRange>()

        val virgulaSeguidaDeOperador = Regex("""[,][$REGEX_OPERADORES]""")
        val operadoresSeguidosDeVirgula = Regex("""[$REGEX_OPERADORES][,]""")
        val virgulaNoComeco = Regex("""^[,]""")
        val virgulaNoFim = Regex("""[,]$""")
        val opNoComeco = Regex("""^[${REGEX_OPERADORES_SEM_SUBT}]""")// o sinal - pode ficar no começo da formula
        val opNoFim = Regex("""[$REGEX_OPERADORES]$""")

        val padroes = arrayListOf(virgulaSeguidaDeOperador, operadoresSeguidosDeVirgula, virgulaNoComeco, virgulaNoFim, opNoComeco, opNoFim)

        padroes.forEach { padrao -> padrao.findAll(formula).forEach { areaErros.add(it.range) } }

        val spannableStr = SpannableString(formula)

        areaErros.forEach {
            spannableStr.setSpan(ForegroundColorSpan(colorAccent), it.first, (it.last + 1).coerceAtMost(formula.length), Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        }

        binding.edtValor.setText(spannableStr)
        binding.edtValor.setSelection(spannableStr.toString().length)
        UIUtils.vibrar(UIUtils.Vibracao.ERRO)


        return areaErros.size > 0
    }

    /**
     * Essa função atualiza a interface com a formula corrigida em caso de operadores duplicados
     */
    private fun corrigirOperadoresIlegais() {
        val formula = binding.edtValor.text.toString()

        val formulaCorrigida = removerOperadoresEmSequenciaDaFormula(formula)

        binding.edtValor.setText(formulaCorrigida)
        binding.edtValor.setSelection(formulaCorrigida.length)
    }

    /**
     * Busca por operadores em sequencia na formula e os remove, mantendo o ultimo operador
     * da sequencia removida. Essa função leva em conta os numeros negativos, os mantendo na formula.
     *
     * Essa funcao busca corrigir erros simples que podem ocorrer durante a digitação da formula
     * e nao todos os possiveis erros.
     */
    fun removerOperadoresEmSequenciaDaFormula(formula: String): String {
        if (formula.length <= 1) return formula

        /**
         * Captura um operador seguido por outros operadores, assim um operador sozinho nao é
         * enquadrado no padrao, o padrao nao considera operadores seguidos pelo sinal '-' (subraçao) pois
         * este inbdica que o numero a seguir é negativo
         */
        val regexOpLal = Regex("""[$REGEX_OPERADORES][${REGEX_OPERADORES_SEM_SUBT}]+""")
        var novaFormula = formula

        while (regexOpLal.containsMatchIn(novaFormula)) {
            val match = regexOpLal.find(novaFormula, 0) ?: break
            val substituicao = match.value.last()
            novaFormula = novaFormula.replace(match.value, substituicao.toString())
        }
        return novaFormula
    }

    /**
     * Essa função atualiza a interface com a formula corrigida em caso de virgulas duplicadas
     */
    private fun corrigirVirgulasIlegais() {
        val formula = binding.edtValor.text.toString()

        val formulaCorrigida = removerVirgulasMultiplas(formula)

        binding.edtValor.setText(formulaCorrigida)
        binding.edtValor.setSelection(formulaCorrigida.length)
    }

    /**
     * Essa funcao busca corrigir erros simples que podem ocorrer durante a digitação da formula
     * e nao todos os possiveis erros.
     * @return uma versao da formula onde os numeros nao terao mais do que uma virgula
     */
    fun removerVirgulasMultiplas(formula: String): String {

        if (formula.length <= 1) return formula.replace(",", "")

        val regexOperadores = Regex("""[$REGEX_OPERADORES]""")
        val regexVirgulas = Regex("""[,]+""")
        val numeros = regexOperadores.split(formula)

        var novaFormula = formula

        for (numero in numeros) {
            if (!numero.contains(",")) continue

            val posUltimaVirgula = regexVirgulas.findAll(numero).last().range.last

            var novoNumero = ""
            for (i in numero.indices) {
                val charNum = numero[i].toString()
                if (charNum != "," || i == posUltimaVirgula) novoNumero += charNum
            }

            novaFormula = formula.replace(numero, novoNumero)

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
        } else if (localDeInsercaoFinal != -1) {
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
        corrigirVirgulasIlegais()
    }

    class Calculadora(formula: String) {
        companion object {
            /**
             * Padrao regex para encontrar um numero que pode ou nao ser negativo
             */
            const val GRUPO_DIGITOS_PRE_OP = """[+x÷%-]?([-]?[\d.]+)"""
            const val GRUPO_DIGITOS_POS_OP = """([+x÷%-]?[-]?[\d.]+)"""
            const val REGEX_SOMA = """[\d]?[$OP_SOMA]"""
            const val REGEX_SUBT = """[\d]?[$OP_SUBT]"""
            const val REGEX_MULT = """[\d]?[$OP_MULT]"""
            const val REGEX_DIV = """[\d]?[$OP_DIV]"""
            const val REGEX_PORCE = """[\d]?[$OP_PORCE]"""
        }

        private var novaFormula = ""
            /**

             * Os padroes regex atuais só entendem que um numero é negativo se seu sinal de menos
             * for precedido por outro operador qualquer (+,x,÷,%,-) neste exemplo 25*-35+3/4,
             * entende-se que 35 é negativo ja que o sinal de menos é precedido pelo sinal de multiplicaçao
             * que representa a operaçao a ser feita agora, se o numero da frente é negativo os
             * padroes regex nao conseguem identificar ex: -25*-35+3/4, aqui apenas o 35 é reconhecido
             * como negativo.
             *
             * Esse setter adiciona uma sinal de soma a uma formula que começe com
             * um numero negativo para que os padroes regex consigam identificar que o
             * sinal de menos significa que o primeiro numero da formula é negativo.
             */
            set(value) {

                // TODO: ATENÇAO OPTEI POR DESCONTINUAR ESSA VERSAO DA CALCULADORA QUE RESOLVE FORMULAS PQ TA DANDO BASTANTE TRABALHO E O APP NAO PRECISA DE TUDO ISSO

                // TODO: qdo a formula estiver totalmente calculada nao se deve adicionar um + na frente pois caga o resultado 
                // TODO: use 10,1x-2,3÷1,04+-0,5-0,01÷-5,2 para testar se deu tudo certo no navegador. copie a formula do wpp para inserir no app
                // TODO: crie uma função para validar o valor recebido aqui e setar ele adequadamente, nao faça isso aqui no setter 
                field = if (Regex(REGEX_OPERADORES).findAll(value).count() <= 1) {
                    //se a formula tem apenas um ou nenhum operador ela ja foi calculada e por tanto
                    // nao se deve adicionar um sinal de mais na frente dela se for negativa
                    value
                } else if (value.startsWith(OP_SUBT)) "$OP_SOMA$value"
                else value
            }
        private val mathContext = MathContext(8, RoundingMode.HALF_DOWN)


        init {
            novaFormula = formula.replace(",", ".")
        }

        /**
         * Faz o calculo da formula na ordem correta:
         *  multiplicacoes e divisoes primeiro, depois somas e subtraçoes, por ultimo, porcentagens
         *  @throws NumberFormatException se a formula for invalida
         */
        fun calcular(): String {

            //Log.d("USUK", "Calculadora.calcular: $novaFormula - formula")
            calcularMultiPlicacoes()
            //Log.d("USUK", "Calculadora.calcular: $novaFormula - pos mult")
            calcularDivisoes()
            //Log.d("USUK", "Calculadora.calcular: $novaFormula - pos div")
            calcularSomas()
            //Log.d("USUK", "Calculadora.calcular: $novaFormula - pos somas")
            calcularSubtracoes()
            //Log.d("USUK", "Calculadora.calcular: $novaFormula - pos subs")
            calcularPorcentagens()
            //Log.d("USUK", "Calculadora.calcular: $novaFormula - pos porcents")

            /*se o resultado da formula for negativo, um sinal de + sera adicionado na frente do resultado
            * (veja o setter da variavel novaFormula), esse sinal de soma deve ser removido ao fim dos calculos*/
            return if (novaFormula.startsWith("$OP_SOMA$OP_SUBT")) novaFormula.drop(1) else novaFormula
        }

        private fun calcularPorcentagens() {
            if (!novaFormula.contains(OP_PORCE)) return

            val regex = Regex("""$GRUPO_DIGITOS_PRE_OP$REGEX_PORCE$GRUPO_DIGITOS_POS_OP""")

            while (regex.containsMatchIn(novaFormula)) {
                val match = regex.find(novaFormula, 0)!!
                val valor1 = match.groups[1]!!.value
                val valor2 = match.groups[2]!!.value
                val resultado = valor1.toBigDecimal().multiply(valor2.toBigDecimal(), mathContext).divide("100".toBigDecimal(), mathContext)
                novaFormula = novaFormula.replace(match.value, resultado.toString())
                Log.d("USUK", "Calculadora: $novaFormula")
            }
        }

        private fun calcularSubtracoes() {
            if (!novaFormula.contains(OP_SUBT)) return

            val regex = Regex("""$GRUPO_DIGITOS_PRE_OP$REGEX_SUBT$GRUPO_DIGITOS_POS_OP""")

            while (regex.containsMatchIn(novaFormula)) {
                val match = regex.find(novaFormula, 0)!!
                val valor1 = match.groups[1]!!.value
                val valor2 = match.groups[2]!!.value
                val resultado = valor1.toBigDecimal().subtract(valor2.toBigDecimal(), mathContext)
                novaFormula = novaFormula.replace(match.value, resultado.toString())
                Log.d("USUK", "Calculadora: $novaFormula")
            }
        }

        private fun calcularSomas() {
            if (!novaFormula.contains(OP_SOMA)) return

            val regex = Regex("""$GRUPO_DIGITOS_PRE_OP$REGEX_SOMA$GRUPO_DIGITOS_POS_OP""")

            while (regex.containsMatchIn(novaFormula)) {
                val match = regex.find(novaFormula, 0)!!
                val valor1 = match.groups[1]!!.value
                val valor2 = match.groups[2]!!.value
                val resultado = valor1.toBigDecimal().add(valor2.toBigDecimal(), mathContext)
                novaFormula = novaFormula.replace(match.value, resultado.toString())
                Log.d("USUK", "Calculadora: $novaFormula")
            }
        }

        private fun calcularDivisoes() {
            if (!novaFormula.contains(OP_DIV)) return

            val regex = Regex("""$GRUPO_DIGITOS_PRE_OP$REGEX_DIV$GRUPO_DIGITOS_POS_OP""")
            while (regex.containsMatchIn(novaFormula)) {
                val match = regex.find(novaFormula, 0)!!
                val valor1 = match.groups[1]!!.value
                val valor2 = match.groups[2]!!.value
                val resultado = valor1.toBigDecimal().divide(valor2.toBigDecimal(), mathContext)
                novaFormula = novaFormula.replace(match.value, resultado.toString())
                Log.d("USUK", "Calculadora: $novaFormula")
            }
        }

        private fun calcularMultiPlicacoes() {

            if (!novaFormula.contains(OP_MULT)) return

            val regex = Regex("""$GRUPO_DIGITOS_PRE_OP$REGEX_MULT$GRUPO_DIGITOS_POS_OP""")

            while (regex.containsMatchIn(novaFormula)) {
                val match = regex.find(novaFormula, 0)!!
                val valor1 = match.groups[1]!!.value
                val valor2 = match.groups[2]!!.value
                val resultado = valor1.toBigDecimal().multiply(valor2.toBigDecimal(), mathContext)
                novaFormula = novaFormula.replace(match.value, resultado.toString())
                Log.d("USUK", "Calculadora: $novaFormula")
            }

        }
    }

}
