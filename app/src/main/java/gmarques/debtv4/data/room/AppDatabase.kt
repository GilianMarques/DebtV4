package gmarques.debtv4.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import gmarques.debtv4.data.room.dao.DespesaDao
import gmarques.debtv4.data.room.entidades.DespesaEntidade

const val DATABASE_NAME = "app-database.sql"

@Database(
    version = 1,
    exportSchema = false,
    entities = [DespesaEntidade::class]
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getDespesaDao(): DespesaDao

   }