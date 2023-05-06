package gmarques.debtv4.data


import android.util.Log
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

        val jsonString = JSONObject(jsonSerializador.toJSon(mDespesa))
            .toString()
        return jsonSerializador.fromJson(jsonString, DespesaEntidade::class.java)
    }

    fun getDespesaRecorrenteEntidade(mDespesa: DespesaRecorrente): DespesaRecorrenteEntidade {

        val jsonString = JSONObject(jsonSerializador.toJSon(mDespesa))
            .toString()

        return jsonSerializador.fromJson(jsonString, DespesaRecorrenteEntidade::class.java)
    }

    fun getDespesa(mDespesa: DespesaEntidade): Despesa {

        val jsonString = JSONObject(jsonSerializador.toJSon(mDespesa))
            .toString()

        return jsonSerializador.fromJson(jsonString, Despesa::class.java)
    }

    fun getDespesaRecorrente(despesa: Despesa): DespesaRecorrente {
        val jsonString = JSONObject(jsonSerializador.toJSon(despesa))
            .toString()

        return jsonSerializador.fromJson(jsonString, DespesaRecorrente::class.java)
    }

    /**
     * Cria uma [Despesa] com os mesmos dados da despesa recebida, porem com uma uid diferente
     */
    fun clonarDespesa(despesa: Despesa): Despesa {
        val jsonString = JSONObject(jsonSerializador.toJSon(despesa))
            .put("uid", Despesa().uid)
            .toString()

        return jsonSerializador.fromJson(jsonString, Despesa::class.java)
    }


}