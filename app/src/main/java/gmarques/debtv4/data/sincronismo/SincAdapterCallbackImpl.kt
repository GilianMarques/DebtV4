package gmarques.debtv4.data.sincronismo

import gmarques.debtv4.data.Mapper
import gmarques.debtv4.data.firebase.cloud_firestore.DespesaDaoFireBase
import gmarques.debtv4.data.room.dao.DespesaDaoRoom
import gmarques.debtv4.data.sincronismo.api.Callback
import gmarques.debtv4.data.sincronismo.api.SincAdapter
import gmarques.debtv4.data.sincronismo.api.Sincronizavel
import gmarques.debtv4.data.sincronismo.api.UICallback
import gmarques.debtv4.domain.entidades.Despesa
import javax.inject.Inject

class SincAdapterCallbackImpl @Inject constructor(
    var despesasDaoLocal: DespesaDaoRoom,
    var despesasDaoNuvem: DespesaDaoFireBase,
    var mapper: Mapper,
) : Callback {

    lateinit var uiCallback: UICallback

    suspend fun executar() {
        SincAdapter(this).executar()
    }


    override suspend fun getDadosLocal(): ArrayList<Sincronizavel> {
        val dados = ArrayList<Sincronizavel>(despesasDaoLocal.getTodosObjetos().map { mapper.getDespesa(it) })
        uiCallback.status("Carregando...", "dados da locais carregados " + dados.size)
        return dados
    }

    override suspend fun getDadosNuvem(): ArrayList<Sincronizavel> {
        val dados = ArrayList<Sincronizavel>(despesasDaoNuvem.getTodosObjetos())
        uiCallback.status("Carregando...", "dados da nuvem carregados " + dados.size)
        return dados
    }

    override suspend fun removerDefinitivamenteLocal(obj: Sincronizavel) {
        despesasDaoLocal.remover(mapper.getDespesaEntidade(obj as Despesa))
        uiCallback.status("remover", "despesa local ${obj.nome} foi removida permanentemente")
    }

    override suspend fun removerDefinitivamenteNuvem(obj: Sincronizavel) {
        despesasDaoNuvem.remover(obj as Despesa)
        uiCallback.status("remover", "despesa nuvem ${obj.nome} foi removida permanentemente")
    }

    override suspend fun atualizarObjetoLocal(nuvemObj: Sincronizavel) {
        despesasDaoLocal.atualizar(mapper.getDespesaEntidade(nuvemObj as Despesa))
        uiCallback.status("atualizar", "despesa  ${nuvemObj.nome} foi atualizada localmente")
    }

    override suspend fun atualizarObjetoNuvem(localObj: Sincronizavel) {
        despesasDaoNuvem.addOuatualizarSincrono(localObj as Despesa)
        uiCallback.status("atualizar", "despesa  ${localObj.nome} foi atualizada na nuvem")
    }

    override suspend fun addNovoObjetoLocal(nuvemObj: Sincronizavel) {
        despesasDaoLocal.addOuAtualizar(mapper.getDespesaEntidade(nuvemObj as Despesa))
        uiCallback.status("adicionar", "despesa  ${nuvemObj.nome} foi atualizada localmente")
    }

    override suspend fun addNovoObjetoNuvem(localObj: Sincronizavel) {
        despesasDaoNuvem.addOuatualizarSincrono(localObj as Despesa)
        uiCallback.status("adicionar", "despesa  ${localObj.nome} foi atualizada na nuvem")
    }

    override suspend fun sincronismoConluido() {
        uiCallback.status("feito", "App sincronizado")
        uiCallback.feito(true, null)
    }
}