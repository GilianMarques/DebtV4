package gmarques.debtv4.data.room.entidades

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "despesas")
/**
 * Entidade da classe [gmarques.debtv4.domain.entidades.Despesa]
 * @see gmarques.debtv4.domain.entidades.Despesa
 */
open class DespesaEntidade(

    @PrimaryKey @ColumnInfo(name = "uid") val uid: String,

    @ColumnInfo(name = "nome") val nome: String,

    @ColumnInfo(name = "valor") val valor: Double,

    @ColumnInfo(name = "esta_paga") val estaPaga: Boolean,

    @ColumnInfo(name = "data_do_pagamento") val dataDoPagamento: Long,

    @ColumnInfo(name = "data_em_que_foi_paga") val dataEmQueFoiPaga: Long,

    @ColumnInfo(name = "observacoes") val observacoes: String,

    @ColumnInfo(name = "foi_removida") val foiRemovida: Boolean,

    @ColumnInfo(name = "ultima_atualizacao") val ultimaAtualizacao: Long,
)