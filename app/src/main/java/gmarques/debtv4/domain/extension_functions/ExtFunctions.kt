package gmarques.debtv4.domain.extension_functions

import android.content.res.Resources
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.roundToInt

class ExtFunctions {

    companion object {
        /**
         * @return quantos % 'A' é de 'B' on de 'A' é o inteiro no qual a funçao esta sendo chamada e
         * 'B' é o parametro recebido.
         * Exemplo: Se 'A' é = 30 e 'B' é = 200 o retorno sera 15.0
         * */
        fun Int.porcentoDe(alvo: Int): Float {
            return BigDecimal(this)
                .divide(BigDecimal(alvo), MathContext(6, RoundingMode.UP))
                .multiply(BigDecimal(100))
                .toFloat()
        }



        fun Int.dp(): Int {
            return (this * Resources.getSystem().displayMetrics.density).roundToInt()
        }

        fun Float.dp(): Int {
            return (this * Resources.getSystem().displayMetrics.density).roundToInt()
        }
    }
}