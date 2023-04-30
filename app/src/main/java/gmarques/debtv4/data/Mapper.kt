package gmarques.debtv4.data


import gmarques.debtv4.data.room.entidades.DespesaEntidade
import gmarques.debtv4.domain._interfaces.JsonSerializador
import gmarques.debtv4.domain.entidades.Despesa
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class Mapper @Inject constructor(private val jsonSerializador: JsonSerializador) {


    suspend fun getDespesaEntidade(mDespesa: Despesa): DespesaEntidade = withContext(IO) {

        val jsonString = JSONObject(jsonSerializador.toJSon(mDespesa))
            .toString()

        jsonSerializador.fromJson(jsonString, DespesaEntidade::class.java)
    }


    suspend fun getDespesaEntidade(mDespesa: DespesaEntidade): Despesa = withContext(IO) {

        val jsonString = JSONObject(jsonSerializador.toJSon(mDespesa))
            .toString()

        jsonSerializador.fromJson(jsonString, Despesa::class.java)
    }


}