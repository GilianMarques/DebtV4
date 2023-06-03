package gmarques.debtv4.domain.entidades

import gmarques.debtv4.data.sincronismo.api.Sincronizavel


/**
 * Versao de dominio do objeto despesa. Alteraçoes nos campos dessa classe
 * devem se refletir na sua entidade e classes que a herdam.
 * @see gmarques.debtv4.data.room.entidades.DespesaEntidade
 * @see Recorrencia
 */
open class Despesa : Sincronizavel() {

    companion object {
        const val VALOR_MAXIMO = 9_999_999.99 // 10 milhoes -R$0,01
        const val VALOR_MINIMO = 0.01
        const val COMPRIMENTO_MAXIMO_NOME = 35
        const val COMPRIMENTO_MAXIMO_OBSERVACOES = 250
    }

    var nome = ""
    var valor = 0.0

    var estaPaga = false
        set(value) {
            field = value
            if (!value) dataEmQueFoiPaga = 0L
        }

    var dataDoPagamento = 0L //utc
    var dataEmQueFoiPaga: Long? = null //utc
    var observacoes = ""

}