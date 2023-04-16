package gmarques.debtv4.presenter.add_despesa

import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButtonToggleGroup
import gmarques.debtv4.R
import gmarques.debtv4.databinding.LayoutBsRepetirDespesaBinding
import gmarques.debtv4.domain.entidades.Recorrencia
import gmarques.debtv4.domain.entidades.Recorrencia.Tipo.*
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
    private val callback: Callback,
    private val fragmento: CustomFrag,
    private var qtdRepeticoes: Int = 0,
    private var tipoRecorrencia: Recorrencia.Tipo? = MESES,
) {

    private var dialogo: BetterBottomSheet = BetterBottomSheet()
    private var binding: LayoutBsRepetirDespesaBinding = LayoutBsRepetirDespesaBinding.inflate(fragmento.layoutInflater)

    init {
        initBotoes()
        carregarViewsComDadosRecebidos()
        mostrarTeclado()
        initEdtIntervalo()
        initBotoesTipoRecorrencia()
        binding.toggleButton.check(binding.meses.id)
    }

    /**
     * Atualiza a variavel [tipoRecorrencia] sempre que um botao de tipo é selecionado
     */
    private fun initBotoesTipoRecorrencia() {

        binding.toggleButton.addOnButtonCheckedListener { a: MaterialButtonToggleGroup, _: Int, _: Boolean ->

            when (a.checkedButtonId) {
                R.id.meses -> tipoRecorrencia = MESES
                R.id.dias  -> tipoRecorrencia = DIAS
            }

            atualizarDica()
        }
    }

    /**
     * Atualiza a variavel [qtdRepeticoes] sempre que o usuario atualiza o valor na tela
     */
    private fun initEdtIntervalo() {
        binding.edtRepetir.addTextChangedListener {


            qtdRepeticoes = it.toString().ifEmpty { "0" }.toInt()
            atualizarDica()
        }
    }

    /**
     * mostra no textview como vai ser o intervalo da despesa
     */
    private fun atualizarDica() {

        val texto = when (tipoRecorrencia) {

            MESES -> {
                when (qtdRepeticoes) {
                    0    -> fragmento.getString(R.string.A_despesa_se_repete_todos_os_meses)
                    1    -> fragmento.getString(R.string.A_despesa_se_repete_mes_sim_e_mes_nao)
                    else -> String.format(fragmento.getString(R.string.A_despesa_se_repete_a_cada_x_meses), qtdRepeticoes)
                }
            }

            DIAS  -> {
                when (qtdRepeticoes) {
                    0    -> fragmento.getString(R.string.A_despesa_se_repete_todos_os_dias)
                    1    -> fragmento.getString(R.string.A_despesa_se_repete_dia_sim_e_dia_nao)
                    else -> String.format(fragmento.getString(R.string.A_despesa_se_repete_a_cada_x_dias), qtdRepeticoes)
                }
            }

            null  -> fragmento.getString(R.string.Selecione_um_tipo_de_recorrencia)
        }

        binding.tvDica.text = texto

    }

    private fun carregarViewsComDadosRecebidos() {

        binding.edtRepetir.setText(qtdRepeticoes.toString())

        tipoRecorrencia?.let {
            when (tipoRecorrencia) {
                MESES -> binding.toggleButton.check(binding.meses.id)
                DIAS  -> binding.toggleButton.check(binding.dias.id)
                else  -> {}
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
        callback.concluido(-1, null)
        dialogo.dismiss()
    }

    private fun validarEntradasUsuarioeFechar() {

        val tipoValido = validarTipoRepeticao()
        if (!tipoValido) return

        val qtdValida = validarQtdRepeticoes()
        if (!qtdValida) return


        callback.concluido(qtdRepeticoes, tipoRecorrencia)
        dialogo.dismiss()
    }

    /**
     * Verifica se o usuario selecionou uma das opçoes de recorrencia da interface e notifica se
     * nao tiver feito
     * @return true se a entrada for valida
     */
    private fun validarTipoRepeticao(): Boolean {
        return if (binding.toggleButton.checkedButtonId == View.NO_ID) {
            fragmento.notificarErro(binding.toggleButton, fragmento.getString(R.string.Selecione_dias_ou_meses_para_prosseguir))
            false
        } else true
    }

    /**
     * verifica se a entrada do usuario nao é nula, vazia ou maior que o permitido
     * @return true se a entrada for valida
     */
    private fun validarQtdRepeticoes(): Boolean {

        if (binding.edtRepetir.text.isNullOrEmpty()) {
            fragmento.notificarErro(binding.edtRepetir, fragmento.getString(R.string.Entrada_invalida))
            return false
        }

        when (tipoRecorrencia) {

            MESES -> {
                if (qtdRepeticoes > Recorrencia.INTERVALO_MAX_REPETICAO_MESES) {
                    fragmento.notificarErro(binding.edtRepetir, String.format(fragmento.getString(R.string.Intervalo_entre_meses_nao_pode_ser_maior_que_x), Recorrencia.INTERVALO_MAX_REPETICAO_MESES))
                    return false
                }
            }

            DIAS  -> {
                if (qtdRepeticoes > Recorrencia.INTERVALO_MAX_REPETICAO_DIAS) {
                    fragmento.notificarErro(binding.edtRepetir, String.format(fragmento.getString(R.string.Intervalo_entre_dias_nao_pode_ser_maior_que_x), Recorrencia.INTERVALO_MAX_REPETICAO_DIAS))
                    return false
                }
            }

            null  -> {}/*Só vai chegar nessa função se o usuario  selecionou um tipo de recorrencia*/

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

    fun interface Callback {
        fun concluido(qtdRepeticoes: Int, tipoRecorrencia: Recorrencia.Tipo?)
    }
}
