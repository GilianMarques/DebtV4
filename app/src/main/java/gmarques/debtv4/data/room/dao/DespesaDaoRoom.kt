package gmarques.debtv4.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import gmarques.debtv4.data.room.entidades.DespesaEntidade
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

@Dao
abstract class DespesaDaoRoom : BaseDao<DespesaEntidade>() {

    @Query("SELECT * FROM despesas WHERE foi_removida = 0 AND data_do_pagamento >= :inicioPeriodo AND data_do_pagamento <= :finalPeriodo ORDER BY data_do_pagamento")
    /**
     * Emite valores quando qualquer linha da tabela despesas é atualizada independentemente se
     * essa linha atualizada esta inclusa na query ou nao
     */
    abstract fun observar(inicioPeriodo: Long, finalPeriodo: Long): Flow<List<DespesaEntidade>>

    @Query("SELECT * FROM despesas WHERE foi_removida = 0 AND data_do_pagamento >= :inicioPeriodo AND data_do_pagamento <= :finalPeriodo AND nome LIKE :nome ORDER BY data_do_pagamento")


    /**
     * Observa as alterações na tabela de despesas de acordo com os critérios de consulta especificados.
     *
     * @param nome O nome da despesa a ser pesquisada.
     * @param inicioPeriodo A data de início do período de pesquisa.
     * @param finalPeriodo A data de término do período de pesquisa.
     * @return Um [Flow] que emite valores quando qualquer linha da tabela despesas é atualizada, independentemente
     * de essa linha atualizada estar incluída na consulta ou não.
     */
    abstract fun observar(nome: String, inicioPeriodo: Long, finalPeriodo: Long): Flow<List<DespesaEntidade>>


    @Query("SELECT * FROM despesas")
    /**
     * retorna todas as despesas, mesmo as marcadas como removidas
     */
    abstract suspend fun getTodosObjetos(): List<DespesaEntidade>

    @Query("SELECT COUNT(*) FROM despesas")
    abstract suspend fun contar(): Int

}