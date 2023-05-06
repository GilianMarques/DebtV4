package gmarques.debtv4.domain.entidades

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.util.Locale

/**
 * Representa o intervalo de datas em que o app vai trabalhar, pode ser um mes ou
 * um periodo de datas distintas
 */
class Periodo(val inicio: Long, val fim: Long) {


    var nome: String = ""
        private set

    init {
        criarNome()
    }


    private fun criarNome() {

        val inicioDateTime = DateTime(inicio, DateTimeZone.UTC)
        val fimDateTime = DateTime(fim, DateTimeZone.UTC)
        val hoje = DateTime(DateTimeZone.UTC)

        val periodoRepresentaUmMes = inicioDateTime.year == fimDateTime.year && inicioDateTime.monthOfYear == fimDateTime.monthOfYear
        val periodoRepresentaMesesDoMesmoAno = inicioDateTime.year == hoje.year

        nome = if (periodoRepresentaUmMes) inicioDateTime.monthOfYear().getAsText(Locale.getDefault())
        else if (periodoRepresentaMesesDoMesmoAno) formatarPeriodocomMesesDiferentes(inicioDateTime, fimDateTime)
        else formatarPeriodocomMeseseAnosDiferentes(inicioDateTime, fimDateTime)
    }

    /**
     * Formata o período entre duas datas com meses e anos diferentes.
     *
     * @param inicioDateTime A data de início do período, em objeto DateTime.
     * @param fimDateTime A data de fim do período, em objeto DateTime.
     *
     * @return O nome do período formatado.
     *
     * Exemplo de uso:
     *
     * val inicio = DateTime(2022, 1, 1, 0, 0, DateTimeZone.UTC)
     * val fim = DateTime(2023, 1, 31, 23, 59, DateTimeZone.UTC)
     * formatarPeriodocomMeseseAnosDiferentes(inicio, fim) // retorna "Jan/22 - Jan/23"
     */
    private fun formatarPeriodocomMeseseAnosDiferentes(inicioDateTime: DateTime, fimDateTime: DateTime): String {
        return inicioDateTime.monthOfYear().getAsShortText(Locale.getDefault()) + "/" + inicioDateTime.year.toString().takeLast(2) + " - " + fimDateTime.monthOfYear().getAsShortText(Locale.getDefault()) + "/" + fimDateTime.year.toString().takeLast(2)
    }

    /**
     * Formata o período entre duas datas com meses do mesmo ano.
     *
     * @param inicioDateTime A data de início do período, em objeto DateTime.
     * @param fimDateTime A data de fim do período, em objeto DateTime.
     *
     * @return O nome do período formatado.
     *
     * Exemplo de uso:
     *
     * val inicio = DateTime(2022, 1, 1, 0, 0, DateTimeZone.UTC)
     * val fim = DateTime(2022, 12, 31, 23, 59, DateTimeZone.UTC)
     * formatarPeriodocomMesesDiferentes(inicio, fim) // retorna "Jan - Dez"
     */
    private fun formatarPeriodocomMesesDiferentes(inicioDateTime: DateTime, fimDateTime: DateTime): String {
        return inicioDateTime.monthOfYear().getAsShortText(Locale.getDefault()) + " - " + fimDateTime.monthOfYear().getAsShortText(Locale.getDefault())
    }
}