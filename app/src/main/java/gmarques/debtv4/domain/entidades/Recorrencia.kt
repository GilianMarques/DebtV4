package gmarques.debtv4.domain.entidades

import java.util.UUID


class  Recorrencia {
    
    enum class Tipo() {
        MESES, DIAS
    }
    
    companion object {
        /**Esse valor é o maximo que o usuario pode configurar de intervalo entre as repetiçoes das despesas em dias/meses*/
        const val INTERVALO_MAX_REPETICAO_DIAS = 90
        const val INTERVALO_MAX_REPETICAO_MESES = 24
    }
    
    var uid: String = UUID.randomUUID().toString()
    lateinit var tipo: Tipo
}