package gmarques.debtv4.domain.modulos_hilt

import android.content.Context
import androidx.room.Room
import gmarques.debtv4.data.room.dao.DespesaDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import gmarques.debtv4.data.room.AppDatabase
import gmarques.debtv4.data.room.DATABASE_NAME
import javax.inject.Singleton

@Module // indica que a classe é um modulo hilt
@InstallIn(SingletonComponent::class) // indica a visibilidade do modulo nesse caso, a visibilidade é para to-do o app veja mais em https://dagger.dev/hilt/components.html#:~:text=components%20used%20for%20injection
class RoomModule {

    @Provides
    fun provideDespesaDao(db: AppDatabase): DespesaDao {
        return db.getDespesaDao()
    }

    @Provides
    @Singleton  // por padrao o hilt provê uma nova instancia da dependencia a cada vez que é solicitada, quando nao queremos esse comportamento usamos essa anotação
            /**
             * Ao delegar a função de criar uma instancia do Room com o hilt reduzo o codigo na classe 'AppDatabase'
             * que era responsavel por criar e manter essa instancia e deixo o codigo mais consiso
             * @See AppDatabase
             * */
    fun provideAppDataBase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DATABASE_NAME
        ).build()

    }

}