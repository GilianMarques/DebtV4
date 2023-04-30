package gmarques.debtv4.data.repositorios

import gmarques.debtv4.data.Mapper
import gmarques.debtv4.data.firebase.cloud_firestore.DespesaDaoFireStore
import gmarques.debtv4.data.firebase.cloud_firestore.DespesaRecorrenteDaoFireStore
import gmarques.debtv4.data.room.dao.DespesaDao
import gmarques.debtv4.data.room.dao.DespesaRecorrenteDao
import gmarques.debtv4.domain.entidades.Despesa
import gmarques.debtv4.domain.entidades.DespesaRecorrente
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import javax.inject.Inject

// para injetar dependencias que sao interfaces, é necessario criar modulos para ensinar ao hilt como instanciar um objeto
class DespesaRecorrenteRepository @Inject constructor(
    private val roomDao: DespesaRecorrenteDao,
    private val fbDao: DespesaRecorrenteDaoFireStore,
    private val mapper: Mapper,
) {

    suspend fun findAll() = roomDao.findAll()

    suspend fun addDespesaRecorrente(despesa: DespesaRecorrente) = withContext(IO) {
        val entidade = mapper.getDespesaRecorrenteEntidade(despesa)
        fbDao.addDespesaRecorrente(entidade)
        roomDao.addOuAtt(entidade)
    }

}