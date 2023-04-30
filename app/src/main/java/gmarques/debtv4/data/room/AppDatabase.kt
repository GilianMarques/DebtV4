package gmarques.debtv4.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import gmarques.debtv4.data.room.dao.DespesaDao
import gmarques.debtv4.data.room.dao.DespesaRecorrenteDao
import gmarques.debtv4.data.room.entidades.DespesaEntidade
import gmarques.debtv4.data.room.entidades.DespesaRecorrenteEntidade

const val DATABASE_NAME = "app-database.sql"

@Database(
    version = 1,
    exportSchema = false,
    entities = [DespesaEntidade::class, DespesaRecorrenteEntidade::class]
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getDespesaDao(): DespesaDao
    abstract fun getDespesaRecorrenteDao(): DespesaRecorrenteDao

}