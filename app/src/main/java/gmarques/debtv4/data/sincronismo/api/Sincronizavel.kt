package gmarques.debtv4.data.sincronismo.api

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
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
    var ultimaAtualizacao: Long = DateTime(DateTimeZone.UTC).millis

    var foiRemovida = false

    var uid = UUID.randomUUID().toString()
        private set

}