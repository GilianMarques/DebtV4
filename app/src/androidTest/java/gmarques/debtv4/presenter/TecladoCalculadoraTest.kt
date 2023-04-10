package gmarques.debtv4.presenter

import org.junit.Assert
import org.junit.Test

internal class TecladoCalculadoraTest {

    @Test
    fun removerOperadoresEmSequenciaDaFormula() {
        val tcalc = TecladoCalculadora()
        val retorno = tcalc.removerOperadoresEmSequenciaDaFormula("35+67-x5xxx%3x÷754")
        Assert.assertEquals("35+67x5%3÷754", retorno)

    }

    @Test
    fun removerVirgulasIlegais() {
        val tcalc = TecladoCalculadora()
        val retorno = tcalc.removerVirgulasMultiplas("35,45,+35,,98-3,5,78+4")
        Assert.assertEquals("3545,+35,98-35,78+4", retorno)

    }
}