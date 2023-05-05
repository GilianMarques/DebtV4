package gmarques.debtv4.data.sincronismo

import gmarques.debtv4.data.firebase.cloud_firestore.DespesaDaoFireStore
import gmarques.debtv4.data.room.dao.DespesaDaoRoom
import gmarques.debtv4.data.sincronismo.api.Callback
import gmarques.debtv4.data.sincronismo.api.SincAdapter
import gmarques.debtv4.data.sincronismo.api.Sincronizavel
import gmarques.debtv4.data.sincronismo.api.UICallback
import gmarques.debtv4.domain.entidades.Despesa
import javax.inject.Inject

// TODO: chamar essa classe de algum lugar pra testar
class SincAdapterCallbackImpl(private val uiCallback: UICallback) : Callback {

    @Inject
    var despesasDaoLocal: DespesaDaoRoom? = null // TODO: implementar funçoes nessa classe 

    @Inject
    var despesasDaoNuvem: DespesaDaoFireStore? = null


    private val sincAdapter: SincAdapter = SincAdapter(this)

    //servem para o controle do sincronismo
    private var opNuvem = 0
    private var opFalhou = false
    private var causaDaFalha: String? = null

    suspend fun executar() {
        sincAdapter.executar()
    }


    override suspend fun getDadosLocal(): ArrayList<Sincronizavel> {
        val dados = ArrayList<Sincronizavel>(despesasDaoLocal!!.getTodasAsDespesas())
        uiCallback.status("Carregando...", "dados da locais carregados " + dados.size)
        return dados
    }

    override suspend fun getDadosNuvem(): ArrayList<Sincronizavel> {
        val dados = ArrayList<Sincronizavel>(despesasDaoNuvem!!.getTodosObjetos())
        uiCallback.status("Carregando...", "dados da nuvem carregados " + dados.size)
        return dados
    }

    override suspend fun removerDefinitivamenteLocal(obj: Sincronizavel) {
        despesasDaoLocal!!.removerDefinitivamente(obj as Despesa)
        uiCallback.status("remover", "despesa local ${obj.nome} foi removida permanentemente")
    }

    override suspend fun removerDefinitivamenteNuvem(obj: Sincronizavel) {
        despesasDaoNuvem!!.remover(obj as Despesa)
        uiCallback.status("remover", "despesa nuvem ${obj.nome} foi removida permanentemente")
    }

    override suspend fun atualizarObjetoLocal(nuvemObj: Sincronizavel) {
        despesasDaoLocal!!.atualizarDespesa(nuvemObj as Despesa)
        uiCallback.status("atualizar", "despesa  ${nuvemObj.nome} foi atualizada localmente")
    }

    override suspend fun atualizarObjetoNuvem(localObj: Sincronizavel) {
        despesasDaoNuvem!!.addOuatualizarSincrono(localObj as Despesa)
        uiCallback.status("atualizar", "despesa  ${localObj.nome} foi atualizada na nuvem")
    }

    override suspend fun addNovoObjetoLocal(nuvemObj: Sincronizavel) {
        despesasDaoLocal!!.addOuAtualizarDespesa(nuvemObj as Despesa)
        uiCallback.status("adicionar", "despesa  ${nuvemObj.nome} foi atualizada localmente")
    }

    override suspend fun addNovoObjetoNuvem(obj: Sincronizavel) {
        despesasDaoNuvem!!.addOuatualizarSincrono(obj as Despesa)
        uiCallback.status("adicionar", "despesa  ${obj.nome} foi atualizada na nuvem")
    }

    override suspend fun sincronismoConluido() {
        uiCallback.status("feito", "App sincronizado")
        uiCallback.feito(true, null)
    }
}