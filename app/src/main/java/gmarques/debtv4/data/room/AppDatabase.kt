package gmarques.debtv4.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import gmarques.debtv4.data.room.dao.DespesaDaoRoom
import gmarques.debtv4.data.room.dao.DespesaRecorrenteDaoRoom
import gmarques.debtv4.data.room.entidades.DespesaEntidade
import gmarques.debtv4.data.room.entidades.DespesaRecorrenteEntidade

const val DATABASE_NAME = "app-database.sql"

@Database(
    version = 1,
    exportSchema = false,
    entities = [DespesaEntidade::class, DespesaRecorrenteEntidade::class]
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getDespesaDao(): DespesaDaoRoom
    abstract fun getDespesaRecorrenteDao(): DespesaRecorrenteDaoRoom

}