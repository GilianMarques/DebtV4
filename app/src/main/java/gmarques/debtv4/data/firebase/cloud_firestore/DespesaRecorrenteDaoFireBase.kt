package gmarques.debtv4.data.firebase.cloud_firestore

import gmarques.debtv4.data.firebase.auth.CloudFireStoreDb
import gmarques.debtv4.data.room.entidades.DespesaRecorrenteEntidade
import javax.inject.Inject


/**
 * @Author: Gilian Marques
 * @Date: sábado, 30 de abril de 2023 às 17:09
 * Dao para acessar dados na nuvem com o firebase.
 * Sempre que possivel os nomes das funçoes dessa classe devem coincidir com os nomes das
 * funções da DAO do Room correspondente
 *
 * @see gmarques.debtv4.data.room.dao.DespesaRecorrenteDaoRoom
 */
class DespesaRecorrenteDaoFireBase @Inject constructor() {

     fun addOuAtualizar(entidade: DespesaRecorrenteEntidade) {
        CloudFireStoreDb.despesasRecorrentesCollection.document(entidade.uid).set(entidade)
    }
}