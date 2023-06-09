package gmarques.debtv4.domain.entidades

import gmarques.debtv4.data.sincronismo.api.Sincronizavel

/**
 * Um subtipo de despesa que herda todos os seu atributos alem de adicionar novos, necessarios
 * para a aplicação da recorrencia na despesa
 * @see Despesa
 * @see gmarques.debtv4.data.room.entidades.RecorrenciaEntidade
 *
 */
class Recorrencia(): Sincronizavel() {

    enum class Tipo {
        MES, DIA
    }

    companion object {
        /**
         * intervalo entre as repeticoes nao pode ser maior que esse valor para dias
         */
        const val INTERVALO_MAX_REPETICAO_DIAS = 180

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
        const val DATA_LIMITE_IMPORATACAO = 2 // anos

    }

    /**
     * O tipo de intervalo em que o objeto se repete
     */
    var tipoDeRecorrencia: Tipo = Tipo.MES

    /**
     * deve ser um inteiro > 0 ou [INTERVALO_MIN_REPETICAO_MESES]
     */
    var intervaloDasRepeticoes: Int = INTERVALO_MIN_REPETICAO_MESES

    /**
     * deve ser uma data no futuro (dentro do limite especificado) ou [LIMITE_RECORRENCIA_INDEFINIDO]
     */
    var dataLimiteDaRecorrencia: Long = LIMITE_RECORRENCIA_INDEFINIDO

    /**
     * É com base nesse valor que se sabe a qual despesa pertence essa recorrencia.
     * Quando o usuario altera o nome de uma despesa recorrente sem propagar essa alteração para as copias
     * dessa despesa, ela deixa de estar vinculada a essa recorrencia.Se ele altera o nome e aplica
     * a alteração em todas as outras copias da data em questao em diante, entao essa recorrencia
     * recebe o novo valor e nao perde a referencia.
     * Nenhum desses dois cenarios impede importaçao de continuar acontecendo.
     * Quando é necessario importar uma copia recorrente de despesa se busca no banco de dados uma
     * despesa com o exato mesmo nome que consta nessa variavel com a data de pagamento mais no
     * futuro possivel, essa despesa entao é copiada, tem suas datas corrigidas para o mes em questao
     * e é salva no banco de dados com uma nova id.
     */
    var nome = ""
}