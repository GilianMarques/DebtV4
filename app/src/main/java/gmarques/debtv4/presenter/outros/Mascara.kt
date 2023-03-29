package gmarques.debtv4.presenter.outros

import android.text.Editable

import android.text.TextWatcher


class Mascara(private val mascara: String) : TextWatcher {

    private var aplicandoMascara = false
    private var apagandoChars = false

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

        if (compEditavel < mascara.length) {
            if (mascara[compEditavel] != '#') {
                editable.append(mascara[compEditavel])
            } else if (mascara[compEditavel - 1] != '#') {
                editable.insert(compEditavel - 1, mascara, compEditavel - 1, compEditavel)
            }
        }
        aplicandoMascara = false
    }

    companion object {
        fun mascaraData(): Mascara {
            return Mascara("##/##/####")
        }
    }
}