package gmarques.debtv4.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import gmarques.debtv4.data.room.entidades.DespesaEntidade
import kotlinx.coroutines.flow.Flow

@Dao
abstract class DespesaDaoRoom : BaseDao<DespesaEntidade>() {

    @Query("SELECT * FROM despesas")
    abstract suspend fun observar(): Flow<List<DespesaEntidade>> // TODO: add filtro na query

    @Query("SELECT * FROM despesas")
    abstract suspend fun getTodosObjetos(): ArrayList<DespesaEntidade>

    @Query("SELECT COUNT(*) FROM despesas")
    abstract suspend fun contar(): Int

}