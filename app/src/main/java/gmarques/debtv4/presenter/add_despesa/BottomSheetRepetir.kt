package gmarques.debtv4.presenter.add_despesa

import android.view.View
import androidx.lifecycle.lifecycleScope
import gmarques.debtv4.R
import gmarques.debtv4.databinding.LayoutBsRepetirDespesaBinding
import gmarques.debtv4.domain.entidades.Recorrencia
import gmarques.debtv4.domain.extension_functions.ExtensionFunctions.Companion.apenasNumeros
import gmarques.debtv4.presenter.BetterBottomSheet
import gmarques.debtv4.presenter.main.CustomFrag
import gmarques.debtv4.presenter.outros.UIUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @Author: Gilian Marques
 * @Date: quarta-feira, 05 de abril de 2023 às 19:47
 *
 * Coleta os dados referentes a repeticao da despesa
 */
class BottomSheetRepetir(
    private val callback: (Int, String, Recorrencia.Tipo?) -> Any,
    private val fragmento: CustomFrag,
    private val qtdRepeticoes: Int,
    private val tipoRecorrencia: Recorrencia.Tipo?,
                        ) {
    
    private var dialogo: BetterBottomSheet = BetterBottomSheet()
    private var binding: LayoutBsRepetirDespesaBinding =
        LayoutBsRepetirDespesaBinding.inflate(fragmento.layoutInflater)
    
    init {
        initBotoes()
        carregarViewsComDadosRecebidos()
        mostrarTeclado()
        binding.toggleButton.check(binding.meses.id)
    }
    
    private fun carregarViewsComDadosRecebidos() {
        
        if (qtdRepeticoes > 0) binding.edtRepetir.setText(qtdRepeticoes.toString())
        
        tipoRecorrencia?.let {
            when (tipoRecorrencia) {
                Recorrencia.Tipo.MESES -> binding.toggleButton.check(binding.meses.id)
                Recorrencia.Tipo.DIAS  -> binding.toggleButton.check(binding.dias.id)
            }
        }
    }
    
    private fun mostrarTeclado() {
        fragmento.lifecycleScope.launch {
            delay(300)
            UIUtils.mostrarTeclado(binding.edtRepetir)
        }
    }
    
    private fun initBotoes() {
        
        binding.salvar.setOnClickListener {
            
            validarEntradasUsuarioeFechar()
        }
        
        binding.naoRepetir.setOnClickListener {
            cancelar()
        }
    }
    
    private fun cancelar() {
        callback.invoke(0, "0", null)
        dialogo.dismiss()
    }
    
    private fun validarEntradasUsuarioeFechar() {
        
        val texto = binding.edtRepetir.text.toString().apenasNumeros()
        val qtdValida = validarQtdRepeticoes(texto)
        if (!qtdValida) return
        
        val tipoValido = validarTipoRepeticao()
        if (!tipoValido) return
        
        var botaoSelecionado = binding.dias
        var tipoRecorrencia = Recorrencia.Tipo.DIAS
        
        if (binding.toggleButton.checkedButtonId == binding.meses.id) {
            botaoSelecionado = binding.meses
            tipoRecorrencia = Recorrencia.Tipo.MESES
        }
        
        
        callback.invoke(
            texto!!.toInt(), botaoSelecionado.text.toString().lowercase(), tipoRecorrencia
                       )
        dialogo.dismiss()
    }
    
    private fun validarTipoRepeticao(): Boolean {
        if (binding.toggleButton.checkedButtonId == View.NO_ID) {
            fragmento.notificarErro(
                binding.toggleButton,
                fragmento.getString(R.string.Selecione_dias_ou_meses_para_prosseguir)
                                   )
            return false
        }
        return true
    }
    
    /**
     * verifica se a entrada do usuario nao é nula, vazia ou maior/menor que o permitido
     * @return true se a entrada for valida
     */
    // TODO: testar
    private fun validarQtdRepeticoes(texto: String?): Boolean {
        
        if (texto == null || texto.isEmpty() || texto.toInt() <= 0) {
            fragmento.notificarErro(
                binding.edtRepetir, fragmento.getString(R.string.Entrada_invalida)
                                   )
            return false
        }
        
        if (texto.toInt() > Recorrencia.INTERVALO_MAX_REPETICAO) {
            fragmento.notificarErro(
                binding.edtRepetir, String.format(
                    fragmento.getString(R.string.O_valor_maximo_para_esse_campo_e_x),
                    Recorrencia.INTERVALO_MAX_REPETICAO
                                                 )
                                   )
            return false
        }
        
        return true
    }
    
    fun mostrar() {
        
        dialogo.customView(binding.root)
            /*se esse dialogo for cancelavel sera necessario definir um dismiss listener para
            * tirar o foco a view de repetir, senao ocorrera um bug toda vez que o usuario fechar
            * o dialogo sem ser pelos botoes(salvar e cancelar) onde é possivel editar o texto da view livremente*/
            .cancelavel(false).show(fragmento.parentFragmentManager)
    }
}
