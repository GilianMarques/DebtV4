package gmarques.debtv4.domain.extension_functions

import android.content.res.Resources
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import kotlin.math.roundToInt

class ExtensionFunctions {
    
    companion object {
        /**
         * @return quantos % 'A' é de 'B' on de 'A' é o inteiro no qual a funçao esta sendo chamada e
         * 'B' é o parametro recebido.
         * Exemplo: Se 'A' é = 30 e 'B' é = 200 o retorno sera 15.0
         * */
        fun Int.porcentoDe(alvo: Int): Float {
            return BigDecimal(this).divide(BigDecimal(alvo), MathContext(6, RoundingMode.UP))
                .multiply(BigDecimal(100)).toFloat()
        }
        
        fun Int.dp(): Int {
            return (this * Resources.getSystem().displayMetrics.density).roundToInt()
        }
        
        /**
         * remove da string tudo que não é numero
         */
        fun String?.apenasNumeros(): String? {
            return if (this == null) null else Regex("\\D").replace(this, "")
        }
       
    }
}