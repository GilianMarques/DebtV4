package gmarques.debtv4.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import gmarques.debtv4.data.room.entidades.RecorrenciaEntidade

@Dao
abstract class RecorrenciaDaoRoom : BaseDao<RecorrenciaEntidade>() {

    @Query("SELECT * FROM recorrencias")
    abstract fun getTodosObjetos(): List<RecorrenciaEntidade>

    @Query("SELECT COUNT(*) FROM recorrencias")
    abstract fun contar(): Int

    @Query("SELECT * FROM recorrencias WHERE foi_removida = 0 AND nome LIKE :nome")
    abstract suspend fun getPorNome(nome: String): RecorrenciaEntidade?


}