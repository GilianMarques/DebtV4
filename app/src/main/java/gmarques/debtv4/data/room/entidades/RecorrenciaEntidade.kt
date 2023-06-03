package gmarques.debtv4.data.room.entidades

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import gmarques.debtv4.domain.entidades.Recorrencia

@Entity(tableName = "recorrencias")
/**
 * Entidade da classe [gmarques.debtv4.domain.entidades.Recorrencia]
 * @see gmarques.debtv4.domain.entidades.Recorrencia
 */
data class RecorrenciaEntidade(

    @PrimaryKey @ColumnInfo(name = "uid") val uid: String,

    @ColumnInfo(name = "nome") val nome: String,

    @ColumnInfo(name = "tipo_recorrencia") var tipoDeRecorrencia: Recorrencia.Tipo,

    @ColumnInfo(name = "intervalo_das_repeticoes") var intervaloDasRepeticoes: Int,

    @ColumnInfo(name = "data_Limite_da_rcorrencia") var dataLimiteDaRecorrencia: Long,

    @ColumnInfo(name = "foi_removida") val foiRemovida: Boolean,

    @ColumnInfo(name = "ultima_atualizacao") val ultimaAtualizacao: Long,

    )