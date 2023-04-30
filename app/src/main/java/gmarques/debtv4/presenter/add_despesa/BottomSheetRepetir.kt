package gmarques.debtv4.presenter.add_despesa

import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButtonToggleGroup
import gmarques.debtv4.R
import gmarques.debtv4.databinding.LayoutBsRepetirDespesaBinding
import gmarques.debtv4.domain.entidades.Recorrencia
import gmarques.debtv4.domain.entidades.Recorrencia.Companion.INTERVALO_MAX_REPETICAO_DIAS
import gmarques.debtv4.domain.entidades.Recorrencia.Companion.INTERVALO_MAX_REPETICAO_MESES
import gmarques.debtv4.domain.entidades.Recorrencia.Companion.INTERVALO_MIN_REPETICAO_DIAS
import gmarques.debtv4.domain.entidades.Recorrencia.Tipo.*
import gmarques.debtv4.presenter.pop_ups.CustomBottomSheet
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
    private var intervaloRepeticoes: Long = 0L,
    private var tipoRecorrencia: Recorrencia.Tipo? = MESES,
) {

    private var dialogo: CustomBottomSheet = CustomBottomSheet()
    private var binding: LayoutBsRepetirDespesaBinding = LayoutBsRepetirDespesaBinding.inflate(fragmento.layoutInflater)

    init {
        initBotoes()
        initBotoesTipoRecorrencia()
        initEdtIntervalo()
        // TODO: talvez seja melhor nao mostrarTeclado()
        carregarViewsComDadosRecebidos()
    }

    /**
     * Atualiza as variaveis [tipoRecorrencia] e [intervaloRepeticoes] sempre que um botao de tipo é selecionado
     *  e atualiza a interface caso o valor de repetiçoes esteja fora da area adequada para cada tipo de recorrencia
     */
    private fun initBotoesTipoRecorrencia() {

        binding.toggleButton.addOnButtonCheckedListener { a: MaterialButtonToggleGroup, _: Int, _: Boolean ->

            intervaloRepeticoes = binding.edtRepetir.text.toString().ifEmpty { "0" }.toLong()

            when (a.checkedButtonId) {
                R.id.meses -> {
                    tipoRecorrencia = MESES
                    if (intervaloRepeticoes > INTERVALO_MAX_REPETICAO_MESES) intervaloRepeticoes = INTERVALO_MAX_REPETICAO_MESES
                }

                R.id.dias  -> {
                    tipoRecorrencia = DIAS
                    intervaloRepeticoes = if (intervaloRepeticoes > INTERVALO_MAX_REPETICAO_DIAS) INTERVALO_MAX_REPETICAO_DIAS
                    else if (intervaloRepeticoes < INTERVALO_MIN_REPETICAO_DIAS) INTERVALO_MIN_REPETICAO_DIAS
                    else intervaloRepeticoes

                }
            }
            // chama a atualização da dica pelo 'addTextChangedListener'
            binding.edtRepetir.setText(intervaloRepeticoes.toString())
        }
    }

    /**
     * Atualiza a variavel [intervaloRepeticoes] sempre que o usuario atualiza o valor na tela
     */
    private fun initEdtIntervalo() {
        binding.edtRepetir.addTextChangedListener {
            intervaloRepeticoes = it.toString().ifEmpty { "0" }.toLong()
            atualizarDica()
        }
    }

    /**
     * mostra no textview como vai ser o intervalo da despesa
     */
    private fun atualizarDica() {

        val texto = when (tipoRecorrencia) {

            MESES -> {
                when (intervaloRepeticoes) {
                    0L    -> fragmento.getString(R.string.A_despesa_se_repete_todos_os_meses)
                    1L    -> fragmento.getString(R.string.A_despesa_se_repete_mes_sim_e_mes_nao)
                    else -> String.format(fragmento.getString(R.string.A_despesa_se_repete_a_cada_x_meses), intervaloRepeticoes)
                }
            }

            DIAS  -> {
                String.format(fragmento.getString(R.string.A_despesa_se_repete_a_cada_x_dias), intervaloRepeticoes)
            }

            null  -> fragmento.getString(R.string.Selecione_um_tipo_de_recorrencia)
        }

        binding.tvDica.text = texto

    }

    /**
     * Atualiza/Inicializa a interface com os valores recebidos do cliente
     */
    private fun carregarViewsComDadosRecebidos() {

        /**
         * Quando um dos botoes de tipo de recorrencia é checado, o valor de [intervaloRepeticoes] pode ser alterado
         * de acordo com as limitações de cada tipo de recorrencia, por isso é necessario preservar
         * o valor recebido como inicial nessa variavel antes de configurar os botoes
         */
        val rep = intervaloRepeticoes

        when (tipoRecorrencia) {
            null  -> binding.toggleButton.check(binding.meses.id)
            MESES -> binding.toggleButton.check(binding.meses.id)
            DIAS  -> binding.toggleButton.check(binding.dias.id)

        }

        binding.edtRepetir.setText(rep.toString())

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
            naoRepetir()
        }
    }

    private fun naoRepetir() {
        callback.concluido(null, null, fragmento.getString(R.string.Nao_repetir))
        dialogo.dismiss()
    }

    private fun validarEntradasUsuarioeFechar() {

        val tipoValido = validarTipoRepeticao()
        if (!tipoValido) return

        val qtdValida = validarQtdRepeticoes()
        if (!qtdValida) return


        callback.concluido(intervaloRepeticoes, tipoRecorrencia, binding.tvDica.text.toString())
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

        return when (tipoRecorrencia) {
            MESES -> validarQtdRepeticoesMes()
            DIAS  -> validarQtdRepeticoesDia()
            null  -> false/*Só vai chegar nessa função se o usuario  selecionou um tipo de recorrencia*/
        }
    }

    private fun validarQtdRepeticoesMes(): Boolean {
        if (intervaloRepeticoes > INTERVALO_MAX_REPETICAO_MESES) {
            fragmento.notificarErro(binding.edtRepetir, String.format(fragmento.getString(R.string.Intervalo_entre_meses_nao_pode_ser_maior_que_x), INTERVALO_MAX_REPETICAO_MESES))
            return false
        }
        return true
    }

    private fun validarQtdRepeticoesDia(): Boolean {

        if (intervaloRepeticoes > INTERVALO_MAX_REPETICAO_DIAS) {
            fragmento.notificarErro(binding.edtRepetir, String.format(fragmento.getString(R.string.Intervalo_entre_dias_nao_pode_ser_maior_que_x), INTERVALO_MAX_REPETICAO_DIAS))
            return false
        } else if (intervaloRepeticoes < INTERVALO_MIN_REPETICAO_DIAS) {
            fragmento.notificarErro(binding.edtRepetir, String.format(fragmento.getString(R.string.Intervalo_entre_dias_nao_pode_ser_menor_que_x), INTERVALO_MIN_REPETICAO_DIAS))
            return false
        }

        return true
    }

    fun mostrar() {
        /*se esse dialogo for cancelavel sera necessario definir um dismiss listener para
        * tirar o foco a view de repetir, senao ocorrera um bug toda vez que o usuario fechar
        * o dialogo sem ser pelos botoes(salvar e cancelar) onde é possivel editar o texto da view livremente
        * */
        dialogo.customView(binding.root)
            .cancelavel(false)
            .show(fragmento.parentFragmentManager)
    }

    fun interface Callback {
        fun concluido(intervaloRepeticoes: Long?, tipoRecorrencia: Recorrencia.Tipo?, dica: String)
    }
}
