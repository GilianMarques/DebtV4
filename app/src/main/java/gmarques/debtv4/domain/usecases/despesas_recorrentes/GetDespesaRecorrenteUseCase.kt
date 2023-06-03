package gmarques.debtv4.domain.usecases.despesas_recorrentes

import gmarques.debtv4.data.Mapper
import gmarques.debtv4.data.room.dao.RecorrenciaDaoRoom
import gmarques.debtv4.domain.entidades.Recorrencia
import javax.inject.Inject

// para injetar dependencias que sao interfaces, é necessario criar modulos para ensinar ao hilt como instanciar um objeto
class GetRecorrenciaUseCase @Inject constructor(
    private val roomDao: RecorrenciaDaoRoom,
    private val mapper: Mapper,
) {

    suspend operator fun invoke(nome:String): Recorrencia? {
        val despRec = roomDao.getPorNome(nome) ?: return null
        return mapper.getRecorrencia(despRec)
    }

}