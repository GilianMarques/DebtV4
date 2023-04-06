package gmarques.debtv4.presenter.outros

import android.text.Editable

import android.text.TextWatcher
import android.util.Log
import java.time.LocalDate

/**
 * completa o valor do ano quando o usuario esta digitando para facilitar
 */
class MascaraData(private val mascara: String) : TextWatcher {
    
    private var aplicandoMascara = false
    private var apagandoChars = false
    private val ano = LocalDate.now().year.toString()
    override fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {
        apagandoChars = count > after
    }
    
    override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
        /*nao é necessario implementar essa funçao*/
    }
    
    override fun afterTextChanged(editable: Editable) {
        if (aplicandoMascara || apagandoChars) return
        
        aplicandoMascara = true
        
        val compEditavel = editable.length
        if (compEditavel == 0) return
        
        if (compEditavel < mascara.length) {
            if (mascara[compEditavel] != '#') {
                editable.append(mascara[compEditavel])
                if (mascara.length - compEditavel == 5) editable.append(ano)
            } else if (mascara[compEditavel - 1] != '#') {
                Log.d("USUK", "MascaraData.afterTextChanged: ${mascara.length - compEditavel}")
                if (mascara.length - compEditavel == 4) {
                    editable.delete(compEditavel - 1, compEditavel)
                    editable.append(mascara[compEditavel-1])
                    editable.append(ano)
                } else editable.insert(compEditavel - 1, mascara, compEditavel - 1, compEditavel)
            }
        }
        aplicandoMascara = false
    }
    
    companion object {
        fun mascaraData(): MascaraData {
            return MascaraData("##/##/####")
        }
        
        fun mascaraDataMeseAno(): MascaraData {
            return MascaraData("##/####")
        }
    }
}