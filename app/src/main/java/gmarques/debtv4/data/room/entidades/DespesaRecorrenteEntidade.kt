package gmarques.debtv4.data.room.entidades

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import gmarques.debtv4.domain.entidades.DespesaRecorrente
import gmarques.debtv4.domain.entidades.DespesaRecorrente.Companion.INTERVALO_MIN_REPETICAO_MESES
import gmarques.debtv4.domain.entidades.DespesaRecorrente.Companion.LIMITE_RECORRENCIA_INDEFINIDO

@Entity(tableName = "despesas_recorrentes")
/**
 * Entidade da classe [gmarques.debtv4.domain.entidades.DespesaRecorrente]
 * @see gmarques.debtv4.domain.entidades.DespesaRecorrente
 */
data class DespesaRecorrenteEntidade(

    @PrimaryKey @ColumnInfo(name = "uid") val uid: String,

    @ColumnInfo(name = "nome") val nome: String,

    @ColumnInfo(name = "valor") val valor: Double,

    @ColumnInfo(name = "esta_paga") val estaPaga: Boolean,

    @ColumnInfo(name = "data_do_pagamento") val dataDoPagamento: Long,

    @ColumnInfo(name = "data_em_que_foi_paga") val dataEmQueFoiPaga: Long,

    @ColumnInfo(name = "observacoes") val observacoes: String,

    @ColumnInfo(name = "tipo_recorrencia") var tipoDeRecorrencia: DespesaRecorrente.Tipo,

    @ColumnInfo(name = "intervalo_das_repeticoes") var intervaloDasRepeticoes: Int,

    @ColumnInfo(name = "data_Limite_da_rcorrencia") var dataLimiteDaRecorrencia: Long,

    @ColumnInfo(name = "foi_removida") val foiRemovida: Boolean,

    @ColumnInfo(name = "ultima_atualizacao") val ultimaAtualizacao: Long,

)