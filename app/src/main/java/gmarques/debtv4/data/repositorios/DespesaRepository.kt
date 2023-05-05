package gmarques.debtv4.data.repositorios

import gmarques.debtv4.data.Mapper
import gmarques.debtv4.data.firebase.cloud_firestore.DespesaDaoFireStore
import gmarques.debtv4.data.room.dao.DespesaDaoRoom
import gmarques.debtv4.domain.entidades.Despesa
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.withContext
import javax.inject.Inject

// TODO: operaçoes de escrita devem ser enviadas para nuvem no ato se possivel, leituras devem ser feitas localmente.
// para injetar dependencias que sao interfaces, é necessario criar modulos para ensinar ao hilt como instanciar um objeto
class DespesaRepository @Inject constructor(
    private val roomDao: DespesaDaoRoom,
    private val fbDao: DespesaDaoFireStore,
    private val mapper: Mapper,
) {

    suspend fun observarDespesas(): Flow<ArrayList<Despesa>> {
        return roomDao.observar().transform { lista ->
            val despesas = ArrayList<Despesa>()
            lista.forEach { despesas.add(mapper.getDespesa(it)) }
            this.emit(despesas)
        }
    }

    suspend fun getTodasAsDespesas(): List<Despesa> {
        return roomDao.getTodosObjetos().map {
            mapper.getDespesa(it)
        }
    }


    suspend fun addOuAtualizarDespesa(despesa: Despesa) = withContext(IO) {
        val entidade = mapper.getDespesaEntidade(despesa)
        fbDao.addOuAtualizar(despesa)
        roomDao.addOuAtualizar(entidade)
    }

    // TODO: aplicar alteraçoes na nuvem tbm
   suspend fun atualizarDespesa(despesa: Despesa) {
        roomDao.atualizar(mapper.getDespesaEntidade(despesa))
    }

}