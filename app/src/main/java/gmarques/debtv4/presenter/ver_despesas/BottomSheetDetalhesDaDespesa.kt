package gmarques.debtv4.presenter.ver_despesas

import android.view.View.*
import gmarques.debtv4.R
import gmarques.debtv4.databinding.LayoutBsDetalhesDaDespesaBinding
import gmarques.debtv4.domain.entidades.Despesa
import gmarques.debtv4.domain.extension_functions.Datas
import gmarques.debtv4.domain.extension_functions.Datas.Companion.dataFormatada
import gmarques.debtv4.domain.extension_functions.ExtensionFunctions.Companion.emMoeda
import gmarques.debtv4.presenter.main.CustomFrag
import gmarques.debtv4.presenter.pop_ups.CustomBottomSheet

// TODO: ajustar as colorOnPrimary, secondary e accent

class BottomSheetDetalhesDaDespesa(
    private val despesa: Despesa,
    private val fragmento: CustomFrag,
) {

    private var dialogo: CustomBottomSheet = CustomBottomSheet()
    private var binding: LayoutBsDetalhesDaDespesaBinding = LayoutBsDetalhesDaDespesaBinding.inflate(fragmento.layoutInflater)

    init {
        popularUi()
    }

    private fun popularUi() {

        binding.edtNome.setText(despesa.nome)
        binding.edtValor.setText(despesa.valor.toString().emMoeda())
        binding.edtDataPagamento.setText(despesa.dataDoPagamento.dataFormatada(Datas.Mascaras.DD_MM_AAAA))
        binding.edtPaga.setText(if (despesa.estaPaga) fragmento.getString(R.string.Despesa_esta_paga) else fragmento.getString(R.string.Em_aberto))

        binding.edtObservacoes.setText(despesa.observacoes)
        if (despesa.observacoes.isEmpty()) binding.edtObservacoes.visibility = GONE
    }

    fun mostrar() {
        /*se esse dialogo for cancelavel sera necessario definir um dismiss listener para
        * tirar o foco a view de repetir, senao ocorrera um bug toda vez que o usuario fechar
        * o dialogo sem ser pelos botoes(salvar e cancelar) onde é possivel editar o texto da view livremente
        * */
        dialogo.customView(binding.root)
            .cancelavel(true)
            .show(fragmento.parentFragmentManager)
    }
}