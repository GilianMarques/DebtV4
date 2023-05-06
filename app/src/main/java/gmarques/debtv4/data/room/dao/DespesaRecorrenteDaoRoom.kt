package gmarques.debtv4.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import gmarques.debtv4.data.room.entidades.DespesaRecorrenteEntidade

@Dao
abstract class DespesaRecorrenteDaoRoom : BaseDao<DespesaRecorrenteEntidade>() {

    @Query("SELECT * FROM despesas_recorrentes")
    abstract fun getTodosObjetos(): List<DespesaRecorrenteEntidade>

    @Query("SELECT COUNT(*) FROM despesas_recorrentes")
    abstract fun contar(): Int

}