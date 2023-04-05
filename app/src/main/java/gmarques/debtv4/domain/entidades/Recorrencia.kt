package gmarques.debtv4.domain.entidades

import java.util.UUID


class Recorrencia {
    companion object {
        /**Esse valor é o maximo que o usuario pode configurar de intervalo entre as repetiçoes das despesas em dias/meses*/
        const val INTERVALO_MAX_REPETICAO = 99
    }

    var uid = UUID.randomUUID()
}