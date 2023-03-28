package gmarques.debtv4.data.repositorios

import gmarques.debtv4.data.room.dao.DespesaDao
import javax.inject.Inject

// para injetar dependencias que sao interfaces, é necessario criar modulos para ensinar ao hilt como instanciar um objeto
class DespesaRepository @Inject constructor(
    private val dao: DespesaDao,
) {

    suspend fun findAll() = dao.findAll()

}