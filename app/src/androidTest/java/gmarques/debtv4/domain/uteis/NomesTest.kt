package gmarques.debtv4.domain.uteis

import junit.framework.TestCase
import org.junit.Test

internal class NomesTest {

    @Test
    fun aplicarCorrecao() {
        TestCase.assertEquals("nome correto #3", Nomes.aplicarCorrecao( " nome  correto !@#3  $%¨&*()_+°"))
    }
}