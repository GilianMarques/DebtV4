package gmarques.debtv4.data.firebase.auth

import gmarques.debtv4.BuildConfig

/**
 * @Author: Gilian Marques
 * @Date: sábado, 29 de abril de 2023 às 13:19
 */
class CloudFireStoreDb {
    companion object {

        const val APP: String = "app"

        /**
         * diminui as chances de cagada garantindo que as interaçoes em depuração nao afetem
         * os dados em produção
         */
        val AMBIENTE: String = if (BuildConfig.DEBUG) "debug" else "producao"

        /**
         * colecao do banco de dados onde sao salvas todos os usuarios do app  por seus respectivos
         * endereços de email
         */
        const val USUARIOS: String = "usuarios"

        /**
         * colecao do banco de dados onde sao salvas todas as despesas do usuario
         */
        const val DESPESAS: String = "despesas"
    }


}