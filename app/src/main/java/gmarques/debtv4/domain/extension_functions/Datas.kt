package gmarques.debtv4.domain.extension_functions

import gmarques.debtv4.domain.entidades.DespesaRecorrente
import org.joda.time.DateTime
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
        DD_MM_AAAA_H_M_S("dd/MM/yyyy HH:mm:ss"), DD_MM_AAAA("dd/MM/yyyy"), MM_AAAA("MM/yyyy")
    }

    companion object {


        /**
         * Converte um long em uma string formatada de acordo com a mascara recebida
         * Use com System.currentTimeMillis
         */
        fun Long.dataFormatada(mascara: Mascaras): String {
            val dataFormat = SimpleDateFormat(mascara.tipo, Locale.getDefault())
            return dataFormat.format(this)
        }

        /**
         * Converte um long em uma string formatada de acordo com a mascara recebida
         * Use para formatar longs em UTC pro usuario
         */
        fun Long.dataFormatadaComOffset(mascara: Mascaras): String {
            val dataFormat = SimpleDateFormat(mascara.tipo, Locale.getDefault())
            return dataFormat.format(aplicarOffset(this))
        }

        /**
         * Converte uma string em dd/mm/aaaa para um long valido. Faz todas as validaçoes de data
         * para garantir que a data em string é 100% valida e esta dentro dos limites permitidos.
         *
         * @return null se a data nao for valida, um long se for.
         */
        fun String.converterDDMMAAAAparaMillis(): Long? {

            val regex = Regex("""(\d{2})/(\d{2})/(\d{4})""")
            val match = regex.matchEntire(this) ?: return null

            val anoAtual = org.joda.time.LocalDateTime().year

            val dia = match.groups[1]!!.value.toInt()
            val mes = match.groups[2]!!.value.toInt()
            val ano = match.groups[3]!!.value.toInt()

            if (dia < 1 || dia > 31 || mes < 1 || mes > 12 || ano > (anoAtual + DespesaRecorrente.DATA_LIMITE_IMPORATACAO) || ano < (anoAtual - DespesaRecorrente.DATA_LIMITE_IMPORATACAO)) return null

            val data = criarData(1, mes, ano)

            val ultimoDiaMes = data.plusMonths(1).minusDays(1).dayOfMonth

            // exemplo: 31/02/2023 sendo que fevereiro tem 28/29 dias dependendo do ano
            if (dia > ultimoDiaMes) return null

            return data.withDayOfMonth(dia).millis
        }


        /**
         * Converte uma string em mm/aaaa para um long valido. Faz todas as validaçoes de data
         * para garantir que a data em string é 100% valida e esta dentro dos limites permitidos.
         *
         * @return null se a data nao for valida, um long se for.
         */
        fun String.converterMMAAAAparaMillis(): Long? {

            val regex = Regex("""(\d{2})/(\d{4})""")
            val match = regex.matchEntire(this) ?: return null

            val anoAtual = org.joda.time.LocalDateTime().year

            val dia = 1
            val mes = match.groups[1]!!.value.toInt()
            val ano = match.groups[2]!!.value.toInt()

            if (mes < 1 || mes > 12 || ano > (anoAtual + DespesaRecorrente.DATA_LIMITE_IMPORATACAO) || ano < (anoAtual - DespesaRecorrente.DATA_LIMITE_IMPORATACAO)) return null

            return criarData(dia, mes, ano).millis
        }

        /**
         * Aplca o offset do fuso-horario local em um timestamp.
         *
         * Essa função tem como objetivo compensar a diferença entre UTC e fuso-horario local para que a
         * conversão de Long para dd/mm/aaaa saia correta.
         *
         * A data que sai do datapicker é referente ao dia selecionado ás 00h:00m:00s em UTC
         * Quando tento converter esse long para dd/mm/aaaa o [SimpleDateFormat] faz a conversão
         * achando que a data esta no fuso-horario local que no meu caso é GMT-03:00 e a data convertida
         * acaba ficando com 1 dia a menos.
         * Exemplo: ao selecionar "12/04/2023" no picker ele retorna o seguinte Long: "1681257600000" que convertido
         * em data fica "Wed Apr 12 2023 00:00:00" porem apos a conversao a data que fica é
         * "Tue Apr 11 2023 21:00:00", no caso 11/04/2023 (veja as horas)
         *
         */
        fun aplicarOffset(data: Long): Long {
            return data - DateTimeZone.getDefault().getOffset(data)
        }

        private fun criarData(dia: Int, mes: Int, ano: Int) =
            DateTime(DateTimeZone.UTC).withYear(ano).withMonthOfYear(mes).withDayOfMonth(dia).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0)

        /**
         * Retorna uma instância de `DateTime` correspondente ao último momento do último dia do mês
         * ao qual a data original pertence.
         *
         * @return instância de `DateTime` correspondente ao último momento do último dia do mês
         *
         * Exemplo de uso:
         * ```
         * val data = DateTime(2023, 5, 10, 15, 30, 0, 0)
         * val ultimoDiaDoMes = data.finalDoMes() // resultado: 2023-05-31T23:59:59.999-03:00
         * ```
         */
        fun DateTime.finalDoMes(): DateTime {
            return this.dayOfMonth().withMaximumValue()
                .withHourOfDay(23)
                .withMinuteOfHour(59)
                .withSecondOfMinute(59)
                .withMillisOfSecond(999)
        }


        /**
         * Retorna um objeto DateTime representando o primeiro dia do mês da data atual, no início do dia.
         *
         * @return o primeiro dia do mês no início do dia.
         *
         * Exemplo de uso:
         * ```
         * val agora = DateTime.now()
         * val inicioDoMes = agora.inicioDoMes() // exemplo: "2023-05-01T00:00:00.000-03:00"
         * ```
         */
        fun DateTime.inicioDoMes(): DateTime {
            return this.withDayOfMonth(1).withTimeAtStartOfDay()
        }

        /**
         * Retorna o nome abreviado em letras maiúsculas do mês correspondente a um determinado
         * timestamp Unix.
         *
         * @param stamp O timestamp Unix a ser convertido em nome de mês.
         * @return O nome abreviado em letras maiúsculas do mês correspondente ao timestamp Unix.
         *
         * Exemplo de uso:
         * ```
         * val stamp = 1620734400L // 11 de maio de 2021, 00:00:00 UTC
         * val mes = nomeComMesTresLetras(stamp) // mes = "MAI"
         * ```
         */
        fun nomeDoMesAbreviado(stamp: Long): String {
            return DateTime(stamp, DateTimeZone.UTC)
                .monthOfYear()
                .getAsShortText(Locale.getDefault())
                .dropLast(1)
                .uppercase()
        }

        fun nomeDoMes(stamp: Long): String {
            return DateTime(stamp, DateTimeZone.UTC)
                .monthOfYear()
                .getAsText(Locale.getDefault())
        }


    }
}
