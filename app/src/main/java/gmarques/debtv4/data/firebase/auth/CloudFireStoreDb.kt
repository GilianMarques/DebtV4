package gmarques.debtv4.data.firebase.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import gmarques.debtv4.BuildConfig

/**
 * @Author: Gilian Marques
 * @Date: sábado, 29 de abril de 2023 às 13:19
 */
class CloudFireStoreDb {
    companion object {
        private const val APP: String = "app"

        /**
         * diminui as chances de cagada garantindo que as interaçoes em depuração nao afetem
         * os dados em produção
         */
        private val AMBIENTE: String = if (BuildConfig.DEBUG) "debug" else "producao"

        private const val USUARIOS: String = "usuarios"
        private const val DESPESAS: String = "despesas"
        private const val DESPESAS_RECORRENTES: String = "despesas_recorrentes"
        private const val RECORRENCIAS: String = "recorrencias"

        private val email = FirebaseAuth.getInstance().currentUser!!.email!!

        val despesasCollection = Firebase.firestore.collection(CloudFireStoreDb.APP)
            .document(CloudFireStoreDb.AMBIENTE)
            .collection(CloudFireStoreDb.USUARIOS)
            .document(email)
            .collection(CloudFireStoreDb.DESPESAS)

        val despesasRecorrentesCollection = Firebase.firestore.collection(CloudFireStoreDb.APP)
            .document(CloudFireStoreDb.AMBIENTE)
            .collection(CloudFireStoreDb.USUARIOS)
            .document(email)
            .collection(CloudFireStoreDb.DESPESAS_RECORRENTES)

        val recorrenciasCollection = Firebase.firestore.collection(CloudFireStoreDb.APP)
            .document(CloudFireStoreDb.AMBIENTE)
            .collection(CloudFireStoreDb.USUARIOS)
            .document(email)
            .collection(CloudFireStoreDb.RECORRENCIAS)
    }


}