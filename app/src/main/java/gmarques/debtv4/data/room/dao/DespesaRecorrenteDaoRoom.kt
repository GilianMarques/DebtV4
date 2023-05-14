package gmarques.debtv4.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import gmarques.debtv4.data.room.entidades.DespesaEntidade
import gmarques.debtv4.data.room.entidades.DespesaRecorrenteEntidade
import gmarques.debtv4.domain.entidades.DespesaRecorrente

@Dao
abstract class DespesaRecorrenteDaoRoom : BaseDao<DespesaRecorrenteEntidade>() {

    @Query("SELECT * FROM despesas_recorrentes")
    abstract fun getTodosObjetos(): List<DespesaRecorrenteEntidade>

    @Query("SELECT COUNT(*) FROM despesas_recorrentes")
    abstract fun contar(): Int

    @Query("SELECT * FROM despesas_recorrentes WHERE foi_removida = 0 AND nome LIKE :nome")
    abstract suspend fun getPorNome(nome: String): DespesaRecorrenteEntidade?


}