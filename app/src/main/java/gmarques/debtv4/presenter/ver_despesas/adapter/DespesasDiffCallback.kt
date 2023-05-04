package gmarques.debtv4.presenter.ver_despesas.adapter

import androidx.recyclerview.widget.DiffUtil
import gmarques.debtv4.domain.entidades.Despesa

//https://blog.mindorks.com/the-powerful-tool-diff-util-in-recyclerview-android-tutorial

class DespesasDiffCallback(
    private val listaAntiga: ArrayList<Despesa>,
    private val novaLista: ArrayList<Despesa>,
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = listaAntiga.size

    override fun getNewListSize(): Int = novaLista.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return listaAntiga[oldItemPosition].uid == novaLista[newItemPosition].uid
    }

    override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean =
        listaAntiga[oldPosition] === novaLista[newPosition]


    override fun getChangePayload(oldPosition: Int, newPosition: Int): Any? {
        return super.getChangePayload(oldPosition, newPosition)
    }
}