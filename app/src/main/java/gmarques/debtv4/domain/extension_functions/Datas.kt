package gmarques.debtv4.domain.extension_functions

import androidx.annotation.VisibleForTesting
import org.joda.time.DateTimeZone
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * @Author: Gilian Marques
 * @Date: quinta-feira, 06 de abril de 2023 às 20:27
 *
 * Utilidades relacionadas à data para serem usadas em todo_ o projeto
 */
class Datas {

    enum class Mascaras(val tipo: String) {
        DD_MM_AAAA_H_M_S("dd/MM/yyyy HH:mm:ss"),
        DD_MM_AAAA("dd/MM/yyyy"),
        MM_AAAA("MM/yyyy")
    }

    companion object {


        /**
         * Converte uma data em UTC em uma string formatada de acordo com a mascara recebida no Fuso-Horario local.
         * Se a data já estiver no fuso-horario local o resultado sera uma data com -3 horas (tendo o GMT-03:00 como exemplo)
         *
         * Atenção, valores nao inclusos na mascara serao perdidos, Doh!
         * Exemplo: Se a mascara for [Mascaras.DD_MM_AAAA_H_M_S]
         * apenas 'dia/mes/ano hora:minuto:segundo' serao preservados na string ou seja, se tentar converter essa string
         * em long novamente esse long terá seus ultimos 4 digitos diferentes do long que originou essa string
         * já que esses 4 digitos representavam os millis dos segundos que nao ficaram salvos na string
         */
        fun Long.formatarDataEmUtcParaStringLocal(mascara: Mascaras): String {
            val dataFormat = SimpleDateFormat(mascara.tipo, Locale.getDefault())
            return dataFormat.format(this.paradataLocal())
        }

        /**
         * Atenção: Se o objeto já estiver em Fuso-horario local a conversao saira com 3 horas
         * antes do horario correto (considerando o Fuso-Horario Do Brasil)
         *
         * Converte uma data em UTC para o fuso-horario local
         *
         * O calculo soma ao horario em UTC o offset do fuso-horario local, no caso do Fuso-horario nacional, GMT-03:00,
         * o valor somado é '-10800000' , repare que por ser um valor negativo a operação acaba sendo
         * de subtração e nao de soma (+ com - é -). Se o valor ja estiver um fuso-horario local o resultado dessa
         * função estara errado.
         *
         * Essa função deve ser usada internamente para ajudar a formatar um Long em UTC para uma String de
         * data formatada no fuso-horario local. Datas só devem ser convertidas no fuso-horario local
         * para exibir ao usuario, no resto usar datas em UTC
         *
         */
        @VisibleForTesting
        fun Long.paradataLocal(): Long {
            return this + DateTimeZone.getDefault().getOffset(this)

        }

        /**
         * Converte uma data em string no fuso-horario local  para um long em UTC.
         * Se a string for uma data em UTC o Long retornado tara um valor errado.
         * @return null se a string recebida não for uma data valida
         */
        fun String.conveterEmDataUTC(mascara: Mascaras): Long? = try {
            val format = SimpleDateFormat(mascara.tipo, Locale.getDefault())
            format.parse(this)!!.time.emUTC()
        } catch (e: java.lang.Exception) {
            null
        }

        /**
         * Atenção: Se o objeto já estiver em UTC  local a conversao saira errada
         *
         * Converte uma data em fuso-horario local para UTC
         *
         * O calculo Subtrai do horario atual o seu proprio offset, no caso do Fuso-horario nacional, GMT-03:00,
         * o valor subtraido é '-10800000' , repare que por ser um valor negativo a operação acaba sendo
         * de soma e nao de subtração (- com - é +). Se o valor ja estiver um UTC o resultado dessa
         * função estara errado.
         *
         */
        fun Long.emUTC(): Long {
            return this - DateTimeZone.getDefault().getOffset(this)
        }

        /**
         * Ajusta a data recebida do picker pra funcionar no app
         * É uma "solução" meia boca pq tentei ersolvar esse problema por horas e nao consegui...
         * Se nao fizer essa alteração, quando a data é formatada, fica com 1 dia a menos. Esse é um
         * problema de fuso-horario UTC/GMT */
        fun ajustarDataDoPicker(dataEmUTC: Long): Long {
            return dataEmUTC.emUTC().emUTC()
        }


    }
}