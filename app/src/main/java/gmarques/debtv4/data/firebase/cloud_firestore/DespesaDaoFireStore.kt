package gmarques.debtv4.data.firebase.cloud_firestore

import gmarques.debtv4.data.firebase.auth.CloudFireStoreDb
import gmarques.debtv4.data.room.entidades.DespesaEntidade
import javax.inject.Inject

/**
 * @Author: Gilian Marques
 * @Date: sábado, 29 de abril de 2023 às 12:29
 */

class DespesaDaoFireStore @Inject constructor() {

     fun addDespesa(entidade: DespesaEntidade) {
        CloudFireStoreDb.despesasCollection.document(entidade.uid).set(entidade)
    }

}