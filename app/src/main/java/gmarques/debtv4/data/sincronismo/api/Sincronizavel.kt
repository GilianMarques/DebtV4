package gmarques.debtv4.data.sincronismo.api

import java.io.Serializable
import java.util.UUID

/**
 * Todos os objetos que serao sincronizados devem extender essa classe
 */
abstract class Sincronizavel : Serializable {

    /**
     * Use um valor constante independente de fuso-horario para atualizar
     * a variavel atravez deste metodo, ou seja, salve timestamps em UTC.
     */
    var ultimaAtualizacao: Long = 0

    var foiRemovida = false

    var uid = UUID.randomUUID().toString()
        private set

}