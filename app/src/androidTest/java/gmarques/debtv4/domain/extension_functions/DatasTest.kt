package gmarques.debtv4.domain.extension_functions

import android.util.Log
import gmarques.debtv4.domain.extension_functions.Datas.*
import gmarques.debtv4.domain.extension_functions.Datas.Companion.conveterLongMillis
import gmarques.debtv4.domain.extension_functions.Datas.Companion.emUTC
import gmarques.debtv4.domain.extension_functions.Datas.Companion.formatarString
import gmarques.debtv4.domain.extension_functions.Datas.Companion.paradataLocal
import gmarques.debtv4.domain.extension_functions.Datas.Mascaras.*
import junit.framework.TestCase
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
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

            val data = agoraUTCEmMillis.formatarString(it)

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

        val stringLocal = agoraUTCEmMillis.paradataLocal().formatarString(DD_MM_AAAA_H_M_S)
        val agoraUTCEmMillisConvertido = stringLocal.conveterLongMillis(DD_MM_AAAA_H_M_S)?.emUTC()

        TestCase.assertEquals(agoraUTCEmMillisConvertido, agoraUTCEmMillis)

    }

    @Test
    fun conveterEmDataUTCTest2() {

        val dia = 10
        val mes = 10
        val ano = 2023

        var stringLocal = "$dia/$mes/$ano"
        var agoraUTCEmMillisConvertido = stringLocal.conveterLongMillis(DD_MM_AAAA)
        val data = DateTime.now(DateTimeZone.UTC).withMillis(agoraUTCEmMillisConvertido!!)

        Log.d("USUK", "DatasTest.conveterEmDataUTCTest2: $agoraUTCEmMillisConvertido, $data")

        TestCase.assertEquals(dia, data.dayOfMonth)
        TestCase.assertEquals(mes, data.monthOfYear)
        TestCase.assertEquals(ano, data.year)

        var dataInvalida = "13/16/"

        TestCase.assertEquals(null, dataInvalida.conveterLongMillis(DD_MM_AAAA))


    }


}