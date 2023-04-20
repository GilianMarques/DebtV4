package gmarques.debtv4.domain.entidades

import org.joda.time.LocalDateTime
import java.util.UUID


class Recorrencia {

    enum class Tipo() {
        MESES, DIAS
    }

    companion object {
        /**
         * intervalo entre as repeticoes nao pode ser maior que esse valor para dias
         */
        const val INTERVALO_MAX_REPETICAO_DIAS = 90

        /**
         * intervalo entre as repeticoes nao pode ser menor que esse valor para dias
         */
        const val INTERVALO_MIN_REPETICAO_DIAS = 31

        /**
         * intervalo entre as repeticoes nao pode ser maior que esse valor para meses
         */
        const val INTERVALO_MAX_REPETICAO_MESES = 24

        /**
         * esse intervalo repete o objeto todos os meses
         */
        const val INTERVALO_RECORRENTE = 0L

        /**
         * O objeto que tem esse valor como data limite de repetição se repete indeterminadamente
         */
        const val LIMITE_RECORRENCIA_INDEFINIDO = -1L
    }

    var uid: String = UUID.randomUUID().toString()
        private set

    /**
     * O tipo de intervalo em que o objeto se repete
     */
    lateinit var tipo: Tipo

    /**
     * deve ser um inteiro >0 ou [INTERVALO_RECORRENTE]
     */
    val intervaloDasRepeticoes = INTERVALO_RECORRENTE

    /**
     * deve ser uma data no futuro ou [LIMITE_RECORRENCIA_INDEFINIDO]
     */
    val dataLimiteRepeticao: Long = LIMITE_RECORRENCIA_INDEFINIDO

}