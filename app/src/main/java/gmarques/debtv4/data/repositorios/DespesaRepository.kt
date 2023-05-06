package gmarques.debtv4.data.repositorios

import gmarques.debtv4.data.Mapper
import gmarques.debtv4.data.firebase.cloud_firestore.DespesaDaoFireBase
import gmarques.debtv4.data.room.dao.DespesaDaoRoom
import gmarques.debtv4.domain.entidades.Despesa
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform
import javax.inject.Inject

// TODO: remover essa classe
// para injetar dependencias que sao interfaces, é necessario criar modulos para ensinar ao hilt como instanciar um objeto

/**
 * Repositorio de despesas e despesas recorrentes,
 * operaçoes de escrita devem ser enviadas para nuvem no ato se possivel, leituras devem ser feitas localmente.
 *
 */
class DespesaRepository @Inject constructor(
    private val roomDao: DespesaDaoRoom,
    private val fbDao: DespesaDaoFireBase,
    private val mapper: Mapper,
) {

     fun observarDespesas(): Flow<ArrayList<Despesa>> {
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




}