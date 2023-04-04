package gmarques.debtv4.domain.extension_functions

import gmarques.debtv4.domain.extension_functions.ExtensionFunctions.Companion.apenasNumeros
import junit.framework.TestCase
import org.junit.Test

internal class ExtensionFunctionsTest {


    @Test
    fun apenasNumerosTest() {
        val stringA = "a cada 5 dias"
        val stringB = "5"
        TestCase.assertEquals(stringB, stringA.apenasNumeros())
    }
}