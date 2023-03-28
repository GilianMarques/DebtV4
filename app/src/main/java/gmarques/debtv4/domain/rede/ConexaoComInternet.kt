package gmarques.debtv4.domain.rede

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class ConexaoComInternet {

    /**
     * Verifica a conexao do dispositivo com a internet
     * se a solicitação enviada pelo aparelho à nuvem nao tiver retorno dentro de 7 segundos
     * assume-se que o dispositivo esta desconectado.
     * */
    suspend fun estaConectado(callback: Callback) {

        // Dou 7 segundos pra solicitaçao ser concluida, se nao assumo que o usuario esta sem internet
        val conectado = withTimeoutOrNull(7_000L) {
            try {
                val conexao = withContext(IO) {
                    URL("https://clients3.google.com/generate_204")
                        .openConnection()
                } as HttpURLConnection
                conexao.responseCode == 204 && conexao.contentLength == 0

            } catch (e: IOException) {
                e.printStackTrace()
                false
            }
        }

        callback.conclusao(conectado != null)
    }


    interface Callback {
        fun conclusao(conectado: Boolean)
    }
}