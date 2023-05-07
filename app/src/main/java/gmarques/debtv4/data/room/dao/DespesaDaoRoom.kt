package gmarques.debtv4.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import gmarques.debtv4.data.room.entidades.DespesaEntidade
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Dao
abstract class DespesaDaoRoom : BaseDao<DespesaEntidade>() {

    @Query("SELECT * FROM despesas WHERE foi_removida = 0 AND data_do_pagamento >= :inicioPeriodo AND data_do_pagamento <= :finalPeriodo")
    /**
     * Emite valores quando qualquer linha da tabela despesas é atualizada independentemente se
     * essa linha atualizada esta inclusa na query ou nao
     */
    abstract fun observar(inicioPeriodo: Long, finalPeriodo: Long): Flow<List<DespesaEntidade>>



    @Query("SELECT * FROM despesas")
    /**
     * retorna todas as despesas, mesmo as marcadas como removidas
     */
    abstract suspend fun getTodosObjetos(): List<DespesaEntidade>

    @Query("SELECT COUNT(*) FROM despesas")
    abstract suspend fun contar(): Int

}