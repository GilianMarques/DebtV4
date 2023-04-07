package gmarques.debtv4.domain.uteis

import android.util.Log
import java.text.SimpleDateFormat
import java.time.*
import java.util.*

/**
 * @Author: Gilian Marques
 * @Date: quinta-feira, 06 de abril de 2023 às 20:27
 *
 * Utilidades relacionadas à data para serem usadas em todo_ o projeto
 */
class DataUtils {
    companion object {

        const val DD_MM_AAAA = "dd/MM/yyyy" // 02/04/2023
        const val MM_AAAA = "MM/yyyy" // 04/2023

        /**
         *  A data retornada pelo datapicker do android vem em UTC
         *  para mostrar ao usuario, é necessario converte-la para o fuso-horario local
         */
        fun corrigirFusoHorario(data: Long): Long {
            val dataUtc = LocalDateTime.ofInstant(Instant.ofEpochMilli(data), ZoneOffset.UTC)
            return ZonedDateTime.of(dataUtc, ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }

        fun agoraEmMillis(): Long {
            return LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault())
                .toInstant(OffsetDateTime.now().offset)
                .toEpochMilli()
                .also { Log.d("USUK", "DataUtils.agoraEmMillis: ${formatarData(it, "dd/MM/yyyy  HH:mm:ss")}  $it") }
        }

        fun formatarData(data: Long, mascara: String): String {
            val dataFormat = SimpleDateFormat(mascara, Locale.getDefault())
            return dataFormat.format(data)
        }

        /**
         * Converte uma data em string para uma representação de millis em long
         */
        fun dataFormatadaParaLong(data: String, mascara: String): Long? = try {
            val format = SimpleDateFormat(mascara, Locale.getDefault())
            format.parse(data)!!.time
        } catch (e: java.lang.Exception) {
            null
        }
    }
}