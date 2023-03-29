package gmarques.debtv4.domain.entidades

import java.util.UUID


class Despesa {
    companion object {
        const val VALOR_MAXIMO = 999_999_99
        const val COMPRIMENTO_MAXIMO_NOME = 35
        const val COMPRIMENTO_MAXIMO_OBSERVACOES = 250
    }

    var uid = UUID.randomUUID()
    var nome = ""
    var valor = 0.0
    var pago = false
    var dataPgto = 0L //utc
    var observacoes = ""
}