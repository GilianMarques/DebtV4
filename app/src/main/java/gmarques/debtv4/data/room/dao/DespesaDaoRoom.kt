package gmarques.debtv4.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import gmarques.debtv4.data.room.entidades.DespesaEntidade
import kotlinx.coroutines.flow.Flow

@Dao
abstract class DespesaDaoRoom : BaseDao<DespesaEntidade>() {

    @Query("SELECT * FROM despesas WHERE foi_removida = 0 AND data_do_pagamento >= :inicioPeriodo AND data_do_pagamento <= :finalPeriodo")
    abstract fun observar(inicioPeriodo: Long, finalPeriodo: Long): Flow<List<DespesaEntidade>> // TODO: add filtro na query

    @Query("SELECT * FROM despesas")
    /**
     * retorna todas as despesas, mesmo as marcadas como removidas
     */
    abstract suspend fun getTodosObjetos(): List<DespesaEntidade>

    @Query("SELECT COUNT(*) FROM despesas")
    abstract suspend fun contar(): Int

}