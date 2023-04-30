package gmarques.debtv4.data.firebase.cloud_firestore

import gmarques.debtv4.data.firebase.auth.CloudFireStoreDb
import gmarques.debtv4.data.room.entidades.DespesaEntidade
import gmarques.debtv4.data.room.entidades.DespesaRecorrenteEntidade
import javax.inject.Inject

/**
 * @Author: Gilian Marques
 * @Date: sábado, 30 de abril de 2023 às 17:09
 */

class DespesaRecorrenteDaoFireStore @Inject constructor() {

     fun addDespesaRecorrente(entidade: DespesaRecorrenteEntidade) {
        CloudFireStoreDb.despesasRecorrentesCollection.document(entidade.uid).set(entidade)
    }
}