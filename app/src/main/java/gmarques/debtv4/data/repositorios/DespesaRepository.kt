package gmarques.debtv4.data.repositorios

import gmarques.debtv4.data.Mapper
import gmarques.debtv4.data.firebase.cloud_firestore.DespesaDaoFireStore
import gmarques.debtv4.data.room.dao.DespesaDao
import gmarques.debtv4.domain.entidades.Despesa
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.withContext
import javax.inject.Inject

// para injetar dependencias que sao interfaces, é necessario criar modulos para ensinar ao hilt como instanciar um objeto
class DespesaRepository @Inject constructor(
    private val roomDao: DespesaDao,
    private val fbDao: DespesaDaoFireStore,
    private val mapper: Mapper,
) {

    suspend fun carregarTodasAsDespesas(): Flow<ArrayList<Despesa>> {
        return roomDao.findAll().transform { lista ->
            val despesas = ArrayList<Despesa>()
            lista.forEach { despesas.add(mapper.getDespesa(it)) }
            this.emit(despesas)
        }
    }


    suspend fun addDespesa(despesa: Despesa) = withContext(IO) {
        val entidade = mapper.getDespesaEntidade(despesa)
        fbDao.addDespesa(entidade)
        roomDao.addOuAtt(entidade)
    }

}