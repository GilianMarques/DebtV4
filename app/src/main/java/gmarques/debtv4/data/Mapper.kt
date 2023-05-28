package gmarques.debtv4.data


import gmarques.debtv4.data.room.entidades.DespesaEntidade
import gmarques.debtv4.data.room.entidades.DespesaRecorrenteEntidade
import gmarques.debtv4.domain._interfaces.JsonSerializador
import gmarques.debtv4.domain.entidades.Despesa
import gmarques.debtv4.domain.entidades.DespesaRecorrente
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class Mapper @Inject constructor(private val jsonSerializador: JsonSerializador) {


    fun getDespesaEntidade(mDespesa: Despesa): DespesaEntidade {

        val jsonString = jsonSerializador.toJSon(mDespesa)
        return jsonSerializador.fromJson(jsonString, DespesaEntidade::class.java)
    }

    fun getDespesaRecorrenteEntidade(mDespesa: DespesaRecorrente): DespesaRecorrenteEntidade {

        val jsonString = jsonSerializador.toJSon(mDespesa)

        return jsonSerializador.fromJson(jsonString, DespesaRecorrenteEntidade::class.java)
    }

    fun getDespesa(mDespesa: DespesaEntidade): Despesa {

        val jsonString = jsonSerializador.toJSon(mDespesa)
        return jsonSerializador.fromJson(jsonString, Despesa::class.java)
    }

    fun getDespesaRecorrente(despesa: Despesa): DespesaRecorrente {
        val jsonString = jsonSerializador.toJSon(despesa)

        return jsonSerializador.fromJson(jsonString, DespesaRecorrente::class.java)
    }

    fun getDespesaRecorrente(despesaRecorrenteEntidade: DespesaRecorrenteEntidade): DespesaRecorrente {
        val jsonString = jsonSerializador.toJSon(despesaRecorrenteEntidade)

        return jsonSerializador.fromJson(jsonString, DespesaRecorrente::class.java)
    }

    /**
     * Cria uma [Despesa] com os mesmos dados da despesa recebida, porem com uma uid diferente
     */
    fun clonarDespesaComOutraId(despesa: Despesa): Despesa {
        val jsonString = emJson(despesa)
            .put("uid", Despesa().uid)
            .toString()

        return jsonSerializador.fromJson(jsonString, Despesa::class.java)
    }

    fun clonarDespesa(despesa: Despesa): Despesa {
        val jsonString = jsonSerializador.toJSon(despesa)

        return jsonSerializador.fromJson(jsonString, Despesa::class.java)
    }

    fun emJson(objeto: Any): JSONObject {
        return JSONObject(jsonSerializador.toJSon(objeto))
    }

    fun <T> getObjeto(jsonString: String, clazz: Class<T>): T {
        return jsonSerializador.fromJson(jsonString, clazz)
    }


}