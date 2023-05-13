package gmarques.debtv4.domain.entidades

/**
 * Um subtipo de despesa que herda todos os seu atributos alem de adicionar novos, necessarios
 * para a aplicação da recorrencia na despesa
 * @see Despesa
 */
class DespesaRecorrente() : Despesa() {

    enum class Tipo {
        MES, DIA
    }

    companion object {
        /**
         * intervalo entre as repeticoes nao pode ser maior que esse valor para dias
         */
        const val INTERVALO_MAX_REPETICAO_DIAS = 90

        /**
         * intervalo entre as repeticoes nao pode ser menor que esse valor para dias
         */
        const val INTERVALO_MIN_REPETICAO_DIAS = 32 // n pode repetir a despesa dentro do mesmo mes

        /**
         * intervalo entre as repeticoes nao pode ser maior que esse valor para meses
         */
        const val INTERVALO_MAX_REPETICAO_MESES = 24

        /**
         * esse intervalo repete o objeto todos os meses
         */
        const val INTERVALO_MIN_REPETICAO_MESES = 0

        /**
         * O objeto que tem esse valor como data limite de repetição se repete indeterminadamente
         */
        const val LIMITE_RECORRENCIA_INDEFINIDO = -1L

        /** a contar da data atual, a quantidade de anos pra frente ou pra tras em que o usuario pode
         * adicionar dados, tambem define um limite para a auto-importação de despesas e receitas no 
         * ato de sua criação
         * */
        const val DATA_LIMITE_IMPORATACAO = 2

    }

    /**
     * O tipo de intervalo em que o objeto se repete
     */
    var tipoDeRecorrencia: Tipo = Tipo.MES

    /**
     * deve ser um inteiro >0 ou [INTERVALO_MIN_REPETICAO_MESES]
     */
    var intervaloDasRepeticoes: Int = INTERVALO_MIN_REPETICAO_MESES.toInt()

    /**
     * deve ser uma data no futuro (dentro do limite especificado) ou [LIMITE_RECORRENCIA_INDEFINIDO]
     */
    var dataLimiteDaRecorrencia: Long = LIMITE_RECORRENCIA_INDEFINIDO


}