package gmarques.debtv4.data.room.entidades

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "despesas")
data class DespesaEntidade(

    @PrimaryKey
    @ColumnInfo(name = "uid") var uid: String,

    @ColumnInfo(name = "nome") var nome: String,

    @ColumnInfo(name = "valor") var valor: Double,

    @ColumnInfo(name = "pago") var pago: Boolean,

    @ColumnInfo(name = "dataPgto") var dataPgto: Long,

    @ColumnInfo(name = "observacoes") var observacoes: String,
)