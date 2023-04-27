package gmarques.debtv4.domain.extension_functions

import gmarques.debtv4.domain.entidades.Recorrencia
import gmarques.debtv4.domain.extension_functions.Datas.Companion.converterDDMMAAAAparaMillis
import gmarques.debtv4.domain.extension_functions.Datas.Companion.converterMMAAAAparaMillis
import gmarques.debtv4.domain.extension_functions.Datas.Companion.formatarString
import gmarques.debtv4.domain.extension_functions.Datas.Companion.formatarStringComOffset
import gmarques.debtv4.domain.extension_functions.Datas.Mascaras.*
import junit.framework.TestCase
import org.joda.time.LocalDateTime
import org.junit.Test

internal class DatasTest {


    @Test
    fun formatarString() {
        val data = 1682467200000.formatarStringComOffset(DD_MM_AAAA)
        TestCase.assertEquals("26/04/2023", data)
    }


    @Test
    fun converterDDMMAAAAparaMillis() {

        val anoAtual = LocalDateTime.now().year
        val anoInvalidoPraMais = anoAtual + Recorrencia.VARIACAO_MAXIMA_DATA + 1
        val anoInvalidoPraMenos = anoAtual - Recorrencia.VARIACAO_MAXIMA_DATA - 1

        val millis = "26/04/2023".converterDDMMAAAAparaMillis()
        TestCase.assertEquals(1682467200000, millis)


        TestCase.assertNull("00/02/2023".converterDDMMAAAAparaMillis())
        TestCase.assertNull("5/2/2023".converterDDMMAAAAparaMillis())
        TestCase.assertNull("05/02/23".converterDDMMAAAAparaMillis())
        TestCase.assertNull("31/02/2023".converterDDMMAAAAparaMillis())
        TestCase.assertNull("01/13/2023".converterDDMMAAAAparaMillis())
        TestCase.assertNull("15/05/$anoInvalidoPraMais".converterDDMMAAAAparaMillis())
        TestCase.assertNull("15/05/$anoInvalidoPraMenos".converterDDMMAAAAparaMillis())
        TestCase.assertNull("".converterDDMMAAAAparaMillis())
        TestCase.assertNull(" ".converterDDMMAAAAparaMillis())
    }

    @Test
    fun converterMMAAAAparaMillis() {

        val anoAtual = LocalDateTime.now().year
        val anoInvalidoPraMais = anoAtual + Recorrencia.VARIACAO_MAXIMA_DATA + 1
        val anoInvalidoPraMenos = anoAtual - Recorrencia.VARIACAO_MAXIMA_DATA - 1

        val millis = "04/2023".converterMMAAAAparaMillis()
        TestCase.assertEquals(1680307200000, millis) // 01/04converterMMAAAAparaMillis()

        TestCase.assertNull("0/2023".converterMMAAAAparaMillis())
        TestCase.assertNull("13/2023".converterMMAAAAparaMillis())
        TestCase.assertNull("02/23".converterMMAAAAparaMillis())
        TestCase.assertNull("05/$anoInvalidoPraMais".converterMMAAAAparaMillis())
        TestCase.assertNull("05/$anoInvalidoPraMenos".converterMMAAAAparaMillis())
        TestCase.assertNull("".converterMMAAAAparaMillis())
        TestCase.assertNull(" ".converterMMAAAAparaMillis())
    }

   }