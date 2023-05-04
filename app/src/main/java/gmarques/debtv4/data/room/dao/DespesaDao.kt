package gmarques.debtv4.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import gmarques.debtv4.data.room.entidades.DespesaEntidade
import kotlinx.coroutines.flow.Flow

@Dao
interface DespesaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addOuAtt(despesas:DespesaEntidade)

    @Query("SELECT * FROM despesas")
    fun findAll(): Flow<List<DespesaEntidade>>

    @Query("SELECT COUNT(*) FROM despesas")
    fun countAll(): Int

}