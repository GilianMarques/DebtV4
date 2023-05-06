package gmarques.debtv4.presenter.ver_despesas.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.AlignSelf
import com.google.android.flexbox.FlexboxLayoutManager
import gmarques.debtv4.databinding.RvDespesasBinding
import gmarques.debtv4.domain.entidades.Despesa
import gmarques.debtv4.domain.extension_functions.Datas
import gmarques.debtv4.domain.extension_functions.Datas.Companion.dataFormatadaComOffset
import gmarques.debtv4.domain.extension_functions.ExtensionFunctions.Companion.emMoeda
import gmarques.debtv4.presenter.ver_despesas.FragVerDespesas

class DespesasAdapter(
    fragListaDeCompras: FragVerDespesas,
    private val callback: DespesasAdapterCallback,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val despesas: ArrayList<Despesa> = ArrayList()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RvDespesasBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, indice: Int) =
        (holder as ViewHolder).bind(despesas[indice], indice)

    override fun getItemCount(): Int = despesas.size

    /**
     * Usa Diffutils para comparar as listas e propagar as alteraçoes automaticamente
     * @throws IllegalArgumentException se o conteudo da nova lista for identico ao da lista atual.
     *
     */
    fun atualizarColecao(novaLista: ArrayList<Despesa>) {

        // se o conteudo das listas antiga e nova são iguais, o conteudo da nova lista é limpo
        // pelo DiffUtil isso faz com que a lista de itens atual fique vazia se tornando inicio
        // de uma grande dor de cabeça. Embora seja possivel verificar se a nova lista esta vazia
        // antes de chamar clear() na lista atual, isso nao deve ser feito por que em nenhum cenario
        // receber uma lista identica a que ja existe é um comportamento desejavel.
        val tamanhonovosItens = novaLista.size

        val mDespesaDiffCallback = DespesasDiffCallback(despesas, novaLista)
        val resultado = DiffUtil.calculateDiff(mDespesaDiffCallback)
        despesas.clear()
        despesas.addAll(novaLista)
        resultado.dispatchUpdatesTo(this)

        if (tamanhonovosItens > 0 && novaLista.size == 0) throw java.lang.IllegalArgumentException(
            "A nova lista tinha $tamanhonovosItens itens e agora tem 0, isso significa que seu " +
                    "conteudo era identico ao conteudo da lista atual do adapter. Corrija isso editando " +
                    "uma copia da lista original, nao a lista original em si.")
    }

    inner class ViewHolder(
        private val bindingView: RvDespesasBinding,
    ) : RecyclerView.ViewHolder(bindingView.root) {

        fun bind(despesa: Despesa, indice: Int) {

            bindingView.tvNome.text = despesa.nome
            bindingView.tvValor.text = despesa.valor.toString().emMoeda()
            bindingView.tvDataPgto.text = despesa.dataDoPagamento.dataFormatadaComOffset(Datas.Mascaras.DD_MM_AAAA)
            bindingView.parentCv.setOnClickListener {
                callback.mostrarBottomSheetResumo(despesa)
            }

            val lp: ViewGroup.LayoutParams = bindingView.parentCv.layoutParams
            if (lp is FlexboxLayoutManager.LayoutParams) {
                lp.flexGrow = 1.0f
                lp.alignSelf = AlignSelf.AUTO
            }
        }


    }
}