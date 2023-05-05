package gmarques.debtv4.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import gmarques.debtv4.data.room.entidades.DespesaRecorrenteEntidade

@Dao
abstract class DespesaRecorrenteDao : BaseDao<DespesaRecorrenteEntidade>() {

    @Query("SELECT * FROM despesas_recorrentes")
    abstract fun getTodosObjetos(): List<DespesaRecorrenteEntidade>

    @Query("SELECT COUNT(*) FROM despesas_recorrentes")
    abstract fun contar(): Int

}