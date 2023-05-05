package gmarques.debtv4.data.firebase.cloud_firestore

import gmarques.debtv4.data.firebase.auth.CloudFireStoreDb
import gmarques.debtv4.domain.entidades.Despesa
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * @Author: Gilian Marques
 * @Date: sábado, 29 de abril de 2023 às 12:29
 */
// TODO: testar 
class DespesaDaoFireStore @Inject constructor() {

    fun addOuAtualizar(despesa: Despesa) {
        CloudFireStoreDb.despesasCollection.document(despesa.uid).set(despesa)
    }

    suspend fun getTodosObjetos(): MutableList<Despesa> {
        return CloudFireStoreDb.despesasCollection.get().await().toObjects(Despesa::class.java)
    }

    /**
     * Remover um documento do firebase nao remove coleçoes e documentos dentro dele
     * veja (link)[https://firebase.google.com/docs/firestore/manage-data/delete-data#kotlin+ktx]
     */
    suspend fun remover(despesa: Despesa) {
        CloudFireStoreDb.despesasCollection.document(despesa.uid).delete().await()
    }

    /**
     * no fim das contas faz o mesmo que [addOuAtualizar]
     * mas de forma sincrona
     * @See addOuAtualizar
     */
    suspend fun addOuatualizarSincrono(despesa: Despesa) {
        CloudFireStoreDb.despesasCollection.document(despesa.uid).set(despesa).await()
    }

}