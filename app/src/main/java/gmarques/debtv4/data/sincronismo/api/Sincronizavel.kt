package gmarques.debtv4.data.sincronismo.api

abstract class Sincronizavel {

    /**
     * Use um valor constante independente de fuso-horario para atualizar
     * a variavel atravez deste metodo, ou seja, salve timestamps em UTC.
     */
    var ultimaAtualizacao: Long = 0

    var foiRemovido = false

    var origem: Long = 0

    abstract fun getUid(): String
}