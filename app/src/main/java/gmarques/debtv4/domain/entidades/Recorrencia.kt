package gmarques.debtv4.domain.entidades

import java.util.UUID


class  Recorrencia {
    
    enum class Tipo() {
        MESES, DIAS
    }
    
    companion object {
        const val INTERVALO_MAX_REPETICAO_DIAS = 90
        const val INTERVALO_MIN_REPETICAO_DIAS = 31
        const val INTERVALO_MAX_REPETICAO_MESES = 24
    }
    
    var uid: String = UUID.randomUUID().toString()
    lateinit var tipo: Tipo
}