package gmarques.debtv4.data.room.entidades

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "despesas")
/**
 * Entidade da classe [gmarques.debtv4.domain.entidades.Despesa]
 * @see gmarques.debtv4.domain.entidades.Despesa
 */
data class DespesaEntidade(

    // TODO: tudo aqui deve ser val
    @PrimaryKey @ColumnInfo(name = "uid") var uid: String,

    @ColumnInfo(name = "nome") var nome: String,

    @ColumnInfo(name = "valor") var valor: Double,

    @ColumnInfo(name = "paga") var paga: Boolean,

    @ColumnInfo(name = "data_do_pagamento") var dataDoPagamento:Long,

    @ColumnInfo(name = "data_em_que_foi_paga") var dataEmQueFoiPaga:Long,

    @ColumnInfo(name = "observacoes") var observacoes: String,
)