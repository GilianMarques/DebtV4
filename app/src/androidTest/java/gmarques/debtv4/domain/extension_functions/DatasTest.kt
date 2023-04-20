package gmarques.debtv4.domain.extension_functions

import gmarques.debtv4.domain.extension_functions.Datas.*
import gmarques.debtv4.domain.extension_functions.Datas.Companion.conveterEmDataUTC
import gmarques.debtv4.domain.extension_functions.Datas.Companion.emUTC
import gmarques.debtv4.domain.extension_functions.Datas.Companion.formatarDataEmUtcParaStringLocal
import gmarques.debtv4.domain.extension_functions.Datas.Companion.paradataLocal
import gmarques.debtv4.domain.extension_functions.Datas.Mascaras.*
import junit.framework.TestCase
import org.joda.time.DateTime
import org.junit.Test

internal class DatasTest {


    @Test

            /**
             * ESSE TESTE SÓ PASSA SE O FUSO-HORARIO LOCAL FOR O DO BRASIL JA QUE O OFFSET ESTA HARDCODED
             */
    fun emUTCTest() {


        val agora = System.currentTimeMillis()
        val utc = agora.emUTC()

        val offsetGmt = -(3 * 60 * 60 * 1000) /*-3 horas*/
        TestCase.assertEquals(agora - offsetGmt, utc)

    }

    @Test

    fun paradataLocalTest() {
        val agora = System.currentTimeMillis()
        val utc = agora.emUTC()
        val agoraconvertido = utc.paradataLocal()

        TestCase.assertEquals(agora, agoraconvertido)
    }

    @Test
            /**
             * A ideia aqui é testar se a funçao esta funcionando com todas as mascaras
             */
    fun formatarDataEmUtcParaStringLocalTest() {

        /** ler doc de [utc.formatarData] para entender pq removo os millis dos segs aqui */
        val agora = DateTime.now().withMillisOfSecond(0)
        val agoraUTCEmMillis = agora.millis.emUTC()

        val dia = agora.dayOfMonth.toString().padStart(2, '0')
        val mes = agora.monthOfYear.toString().padStart(2, '0')
        val ano = agora.year.toString().padStart(2, '0')
        val hora = agora.hourOfDay.toString().padStart(2, '0')
        val mins = agora.minuteOfHour.toString().padStart(2, '0')
        val segs = agora.secondOfMinute.toString().padStart(2, '0')


        // Testo a conversao em todas as mascaras
        Mascaras.values().forEach {

            val data = agoraUTCEmMillis.formatarDataEmUtcParaStringLocal(it)

            when (it) {
                DD_MM_AAAA       -> {
                    TestCase.assertEquals("$dia/$mes/$ano", data)
                }

                DD_MM_AAAA_H_M_S -> {
                    TestCase.assertEquals("$dia/$mes/$ano $hora:$mins:$segs", data)
                }

                MM_AAAA          -> {
                    TestCase.assertEquals("$mes/$ano", data)
                }
            }


        }


    }


    @Test
    fun conveterEmDataUTCTest() {

        /** ler doc de [utc.formatarData] para entender pq removo os millis dos segs aqui */
        val agora = DateTime.now().withMillisOfSecond(0)
        val agoraUTCEmMillis = agora.millis.emUTC()

        val stringLocal = agoraUTCEmMillis.formatarDataEmUtcParaStringLocal(DD_MM_AAAA_H_M_S)
        val agoraUTCEmMillisConvertido = stringLocal.conveterEmDataUTC(DD_MM_AAAA_H_M_S)

        TestCase.assertEquals(agoraUTCEmMillisConvertido, agoraUTCEmMillis)

    }

}