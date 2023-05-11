package gmarques.debtv4.domain.extension_functions

import android.content.res.Resources
import gmarques.debtv4.App
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt


class ExtensionFunctions {

    // TODO: criar sub-classes por tipo

    companion object {
        /**
         * @return quantos % 'A' é de 'B' on de 'A' é o inteiro no qual a funçao esta sendo chamada e
         * 'B' é o parametro recebido.
         * Exemplo: Se 'A' é = 30 e 'B' é = 200 o retorno sera 15.0
         * */
        fun Int.porcentoDe(alvo: Int): Float {
            return BigDecimal(this).divide(BigDecimal(alvo), MathContext(6, RoundingMode.UP)).multiply(BigDecimal(100)).toFloat()
        }

        fun Int.dp(): Int {
            return (this * Resources.getSystem().displayMetrics.density).roundToInt()
        }

        fun Float.dp(): Float {
            return (this * Resources.getSystem().displayMetrics.density)
        }

        /**
         * remove da string tudo que não é numero
         */
        fun String?.apenasNumeros(): String? {
            return if (this == null) null else Regex("""\D""").replace(this, "")
        }

        /**
         * Converte a string em moeda
         * @throws IllegalArgumentException  se a string nao for um valor monetario valido
         * Exemplo de string valida 1520.00
         */
        fun String.emMoeda(): String {
            if (App.demonstracao) "******"//return NumberFormat.getCurrencyInstance(Locale.getDefault()).format(111.11).toString()
            return NumberFormat.getCurrencyInstance(Locale.getDefault()).format(this.toDouble()).toString()
        }

        /**
         * remove o simboloda moeda da string convertida
         */
        fun String.emMoedaSemSimbolo(): String {
            return Regex("""[^\d.,]""").replace(emMoeda(), "")
        }

        /**
         * converte o valor monetario na string em um double porem
         * retorna esse valor como string mesmo
         */
        fun String.emDouble(): String {
            // para um exemplo, considere R$ 10.592,33

            val valorSemVirgula = Regex("""[^\d.,]""").replace(this, "").replace(",", ".") // fica '10.592.33'

            val valorInteiro = valorSemVirgula.dropLast(3) // removo os centavos, resultado: '10.592'
            val valorDecimal = valorSemVirgula.drop(valorSemVirgula.length - 3) // removo os reais, resultado: '.33'

            return "${valorInteiro.replace(".", "")}$valorDecimal"

        }


    }


}