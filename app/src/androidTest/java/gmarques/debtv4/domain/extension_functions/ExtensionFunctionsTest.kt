package gmarques.debtv4.domain.extension_functions

import gmarques.debtv4.domain.extension_functions.ExtensionFunctions.Companion.apenasNumeros
import gmarques.debtv4.domain.extension_functions.ExtensionFunctions.Companion.emDouble
import gmarques.debtv4.domain.extension_functions.ExtensionFunctions.Companion.emMoeda
import junit.framework.TestCase
import org.junit.Test

internal class ExtensionFunctionsTest {


    @Test
    fun apenasNumerosTest() {
        val stringA = "a cada 5. dias"
        val stringB = "5"
        TestCase.assertEquals(stringB, stringA.apenasNumeros())
    }

    @Test
    /**
             * se nao jogar exception, passou!
             */
    fun emMoeda() {
        "25".emMoeda()
        "250.98".emMoeda()
    }

    @Test
    fun emDouble() {
        TestCase.assertEquals("10553.99", "R$10.553,99".emDouble())
        TestCase.assertEquals("10553.99", "R$ 10.553,99".emDouble())
        TestCase.assertEquals("10553.99", "10.553,99".emDouble())
        TestCase.assertEquals("10553.99", "$10.553,99".emDouble())
        TestCase.assertEquals("10553.99", "10.553.99".emDouble())
        TestCase.assertEquals("1.99", "1,99".emDouble())
        TestCase.assertEquals("1.99", "1.99".emDouble())
    }

}