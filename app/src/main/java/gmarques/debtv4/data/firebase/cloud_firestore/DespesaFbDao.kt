package gmarques.debtv4.data.firebase.cloud_firestore

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import gmarques.debtv4.data.firebase.auth.CloudFireStoreDb
import gmarques.debtv4.data.room.entidades.DespesaEntidade
import javax.inject.Inject

/**
 * @Author: Gilian Marques
 * @Date: sábado, 29 de abril de 2023 às 12:29
 */

class DespesaFbDao @Inject constructor() {

    private val db = Firebase.firestore
    private val email = FirebaseAuth.getInstance().currentUser!!.email!!

    private val despesasColection = db.collection(CloudFireStoreDb.APP)
        .document(CloudFireStoreDb.AMBIENTE)
        .collection(CloudFireStoreDb.USUARIOS)
        .document(email)
        .collection(CloudFireStoreDb.DESPESAS)

    suspend fun addDespesa(entidade: DespesaEntidade) {
        despesasColection.document(entidade.uid).set(entidade)

    }
}