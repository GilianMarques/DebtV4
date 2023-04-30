package gmarques.debtv4.domain.entidades

import java.util.UUID


class Recorrencia(tipo: Tipo, intervaloDasRepeticoes: Long, dataLimiteRepeticao: Long) {


    enum class Tipo {
        MESES, DIAS
    }

    companion object {
        /**
         * intervalo entre as repeticoes nao pode ser maior que esse valor para dias
         */
        const val INTERVALO_MAX_REPETICAO_DIAS = 90L

        /**
         * intervalo entre as repeticoes nao pode ser menor que esse valor para dias
         */
        const val INTERVALO_MIN_REPETICAO_DIAS = 31L

        /**
         * intervalo entre as repeticoes nao pode ser maior que esse valor para meses
         */
        const val INTERVALO_MAX_REPETICAO_MESES = 24L

        /**
         * esse intervalo repete o objeto todos os meses
         */
        const val INTERVALO_MIN_REPETICAO_MESES = 0L

        /**
         * O objeto que tem esse valor como data limite de repetição se repete indeterminadamente
         */
        const val LIMITE_RECORRENCIA_INDEFINIDO = -1L

        /** a contar da data atual, a quantidade de anos pra frente ou pra tras em que o usuario pode
         * adicionar despesas*/
        const val VARIACAO_MAXIMA_DATA = 10

    }

    var uid: String = UUID.randomUUID().toString()
        private set

    /**
     * O tipo de intervalo em que o objeto se repete
     */
    var tipo: Tipo
        private set

    /**
     * deve ser um inteiro >0 ou [INTERVALO_MIN_REPETICAO_MESES]
     */
    var intervaloDasRepeticoes = INTERVALO_MIN_REPETICAO_MESES

    /**
     * deve ser uma data no futuro (dentro do limite especificado) ou [LIMITE_RECORRENCIA_INDEFINIDO]
     */
    var dataLimiteRepeticao: Long = LIMITE_RECORRENCIA_INDEFINIDO

    init {
        this.tipo = tipo
        this.intervaloDasRepeticoes = intervaloDasRepeticoes
        this.dataLimiteRepeticao = dataLimiteRepeticao
    }
}