package gmarques.debtv4.domain.entidades

import java.util.UUID


/**
 * Versao de dominio do objeto despesa. Alteraçoes nos campos dessa classe
 * devem se refletir na sua entidade.
 * @see gmarques.debtv4.data.room.entidades.DespesaEntidade
 * @see DespesaRecorrente
 */
open class Despesa {

    companion object {
        const val VALOR_MAXIMO = 9_999_999.99 // 10 milhoes -0,01R$
        const val VALOR_MINIMO = 0.00
        const val COMPRIMENTO_MAXIMO_NOME = 35
        const val COMPRIMENTO_MAXIMO_OBSERVACOES = 250
    }

    var uid = UUID.randomUUID().toString()
        private set
    var nome = ""
    var valor = 0.0
    var estaPaga = false
    var dataDoPagamento = 0L //utc
    var dataEmQueFoiPaga: Long? = null //utc
    var observacoes = ""
}