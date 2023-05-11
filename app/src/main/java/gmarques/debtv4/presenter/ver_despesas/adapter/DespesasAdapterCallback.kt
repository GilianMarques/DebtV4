package gmarques.debtv4.presenter.ver_despesas.adapter

import gmarques.debtv4.domain.entidades.Despesa


fun interface DespesasAdapterCallback {

    fun mostrarResumoDaDespesa(despesa: Despesa)

}