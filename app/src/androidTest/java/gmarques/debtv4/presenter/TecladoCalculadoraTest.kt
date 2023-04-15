package gmarques.debtv4.presenter

import org.junit.Assert
import org.junit.Test

internal class TecladoCalculadoraTest {

    @Test
    fun removerOperadoresEmSequenciaDaFormula() {
        val tcalc = TecladoCalculadora()

        Assert.assertEquals("35+25", tcalc.removerOperadoresEmSequenciaDaFormula("35++25"))
        Assert.assertEquals("35+-25", tcalc.removerOperadoresEmSequenciaDaFormula("35+-25"))
        Assert.assertEquals("35x25", tcalc.removerOperadoresEmSequenciaDaFormula("35-x25"))
        Assert.assertEquals("35÷25", tcalc.removerOperadoresEmSequenciaDaFormula("35x÷25"))
        Assert.assertEquals("35%25", tcalc.removerOperadoresEmSequenciaDaFormula("35÷%25"))
        Assert.assertEquals("35+25", tcalc.removerOperadoresEmSequenciaDaFormula("35%+25"))
        Assert.assertEquals("-35+25", tcalc.removerOperadoresEmSequenciaDaFormula("-35%+25"))

    }

    @Test
    fun removerVirgulasIlegais() {
        val tcalc = TecladoCalculadora()

        Assert.assertEquals("35.0", tcalc.removerPontosMultiplos("35..0"))
        Assert.assertEquals("35.0", tcalc.removerPontosMultiplos(".35.0"))
        Assert.assertEquals("350.", tcalc.removerPontosMultiplos(".35.0."))

    }
    @Test
    fun removerCasasDecimaisIlegais() {
        val tcalc = TecladoCalculadora()

        Assert.assertEquals("35.02+123456.78", tcalc.removerCasasDecimaisIlegais("35.0268+123456.7896"))
        Assert.assertEquals("35.0", tcalc.removerCasasDecimaisIlegais("35.0"))
        Assert.assertEquals("+350.01", tcalc.removerCasasDecimaisIlegais("+350.0123456789789752475"))

    }

    @Test
    fun resultadoValido() {

        val tcalc = TecladoCalculadora()

        Assert.assertTrue(tcalc.resultadoValido("3"))
        Assert.assertTrue(tcalc.resultadoValido("35"))
        Assert.assertTrue(tcalc.resultadoValido("355"))
        Assert.assertTrue(tcalc.resultadoValido("1345.10"))
        Assert.assertTrue(tcalc.resultadoValido("1345.00"))
        Assert.assertTrue(tcalc.resultadoValido("1345"))


        Assert.assertFalse(tcalc.resultadoValido("-1345"))
        Assert.assertFalse(tcalc.resultadoValido("1345.000"))
        Assert.assertFalse(tcalc.resultadoValido("1345,00"))

    }
}