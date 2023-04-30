package gmarques.debtv4.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import gmarques.debtv4.data.room.entidades.DespesaEntidade
import gmarques.debtv4.data.room.entidades.DespesaRecorrenteEntidade

@Dao
interface DespesaRecorrenteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addOuAtt(despesas: DespesaRecorrenteEntidade)

    @Query("SELECT * FROM despesas_recorrentes")
    fun findAll(): List<DespesaRecorrenteEntidade>

    @Query("SELECT COUNT(*) FROM despesas_recorrentes")
    fun countAll(): Int

}