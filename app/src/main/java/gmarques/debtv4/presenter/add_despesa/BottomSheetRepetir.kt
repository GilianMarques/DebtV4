package gmarques.debtv4.presenter.add_despesa

import android.text.InputFilter
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener
import gmarques.debtv4.R
import gmarques.debtv4.databinding.BsRepetirDespesaBinding
import gmarques.debtv4.domain.entidades.Recorrencia
import gmarques.debtv4.domain.entidades.Recorrencia.Companion.INTERVALO_MAX_REPETICAO_DIAS
import gmarques.debtv4.domain.entidades.Recorrencia.Companion.INTERVALO_MAX_REPETICAO_MESES
import gmarques.debtv4.domain.entidades.Recorrencia.Companion.INTERVALO_MIN_REPETICAO_DIAS
import gmarques.debtv4.domain.entidades.Recorrencia.Companion.LIMITE_RECORRENCIA_INDEFINIDO
import gmarques.debtv4.domain.entidades.Recorrencia.Tipo.*
import gmarques.debtv4.domain.extension_functions.Datas
import gmarques.debtv4.domain.extension_functions.Datas.Companion.converterMMAAAAparaMillis
import gmarques.debtv4.domain.extension_functions.Datas.Companion.dataFormatadaComOffset
import gmarques.debtv4.domain.extension_functions.Datas.Companion.mes_De_Ano
import gmarques.debtv4.presenter.main.CustomFrag
import gmarques.debtv4.presenter.outros.MascaraData
import gmarques.debtv4.presenter.pop_ups.CustomBottomSheet

/**
 * @Author: Gilian Marques
 * @Date: quarta-feira, 05 de abril de 2023 às 19:47
 *
 * Coleta os dados referentes a repeticao da despesa
 */
class BottomSheetRepetir(
    private val callback: Callback,
    private val fragmento: CustomFrag,
    private var intervaloDasRepeticoes: Int = 1,
    private var tipoDeRecorrencia: Recorrencia.Tipo? = MES,
    private var dataLimiteDaRepeticao: Long?,
) {
    private val indeterminadamente = fragmento.getString(R.string.Indeterminadamente)
    private var dialogo: CustomBottomSheet = CustomBottomSheet()
    private var binding: BsRepetirDespesaBinding = BsRepetirDespesaBinding.inflate(fragmento.layoutInflater)


    init {
        initBotoes()
        initSpinnerOpcoes()
        initEdtIntervalo()
        initCampoDataLimiteRepeticao()
        carregarViewsComDadosRecebidos()
    }

    /**
     * Atualiza as variaveis [tipoDeRecorrencia] e [intervaloDasRepeticoes] sempre que uma opçao do spinner é selecionada
     *  e atualiza a interface caso o valor de repetiçoes esteja fora da area adequada para cada tipo de recorrencia
     */
    private fun initSpinnerOpcoes() {


        val opcoes = arrayListOf(fragmento.getString(R.string.Mes_es), fragmento.getString(R.string.Dia_s))
        val adapter = ArrayAdapter(fragmento.requireContext(), android.R.layout.simple_spinner_item, opcoes)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerOpcoes.adapter = adapter

        binding.spinnerOpcoes.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {

                when (position) {
                    0 -> {
                        tipoDeRecorrencia = MES
                        if (intervaloDasRepeticoes > INTERVALO_MAX_REPETICAO_MESES) intervaloDasRepeticoes = INTERVALO_MAX_REPETICAO_MESES
                    }

                    1 -> {
                        tipoDeRecorrencia = DIA
                        intervaloDasRepeticoes = if (intervaloDasRepeticoes > INTERVALO_MAX_REPETICAO_DIAS) INTERVALO_MAX_REPETICAO_DIAS
                        else if (intervaloDasRepeticoes < INTERVALO_MIN_REPETICAO_DIAS) INTERVALO_MIN_REPETICAO_DIAS
                        else intervaloDasRepeticoes

                    }
                }
                // chama a atualização da dica pelo 'addTextChangedListener'
                binding.edtRepetir.setText(intervaloDasRepeticoes.toString())


            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                tipoDeRecorrencia = null
                binding.edtRepetir.setText("")
            }
        }
    }

    /**
     * Atualiza a variavel [intervaloDasRepeticoes] sempre que o usuario atualiza o valor na tela
     */
    private fun initEdtIntervalo() {
        binding.edtRepetir.addTextChangedListener {
            intervaloDasRepeticoes = it.toString().ifEmpty { "1" }.toInt()
            atualizarDica()
        }
    }

    /**
     * mostra no textview como vai ser o intervalo da despesa
     */
    private fun atualizarDica() {

        // texto de complemento da dica
        val complementoDaDica = when (dataLimiteDaRepeticao) {
            null                          -> fragmento.getString(R.string.Data_limite_nao_especificada_ou_invalida)
            LIMITE_RECORRENCIA_INDEFINIDO -> indeterminadamente
            else                          -> String.format(fragmento.getString(R.string._ate_x), dataLimiteDaRepeticao!!.mes_De_Ano().lowercase())
        }

        val dica = when (tipoDeRecorrencia) {

            MES  -> {
                when (intervaloDasRepeticoes) {
                    1    -> String.format(fragmento.getString(R.string.A_despesa_se_repete_todos_os_meses_x), complementoDaDica)
                    2    -> String.format(fragmento.getString(R.string.A_despesa_se_repete_mes_sim_e_mes_nao_x), complementoDaDica)
                    else -> String.format(fragmento.getString(R.string.Repetir_a_cada_x_meses_x), intervaloDasRepeticoes, complementoDaDica)

                }
            }

            DIA  -> {
                String.format(fragmento.getString(R.string.Repetir_a_cada_x_dias_x), intervaloDasRepeticoes, complementoDaDica)
            }

            null -> fragmento.getString(R.string.Selecione_um_tipo_de_recorrencia)
        }

        binding.tvDica.text = dica

    }


    /**
     * Atualiza/Inicializa a interface com os valores recebidos do cliente
     */
    private fun carregarViewsComDadosRecebidos() {

        /**
         * Quando um dos botoes de tipo de recorrencia é checado, o valor de [intervaloDasRepeticoes] pode ser alterado
         * de acordo com as limitações de cada tipo de recorrencia, por isso é necessario preservar
         * o valor recebido como inicial nessa variavel antes de configurar os botoes
         */
        val rep = intervaloDasRepeticoes
        when (tipoDeRecorrencia) {
            null -> binding.spinnerOpcoes.setSelection(0)
            MES  -> binding.spinnerOpcoes.setSelection(0)
            DIA  -> binding.spinnerOpcoes.setSelection(1)

        }

        binding.edtRepetir.setText(rep.toString())
        dataLimiteDaRepeticao?.let {
            if (dataLimiteDaRepeticao == LIMITE_RECORRENCIA_INDEFINIDO) binding.edtDataLimiteRepetir.setText(indeterminadamente)
            else binding.edtDataLimiteRepetir.setText(dataLimiteDaRepeticao!!.dataFormatadaComOffset(Datas.Mascaras.MM_AAAA))
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

    private fun initCampoDataLimiteRepeticao() {

        val compMaximMascara = "##/####".length


        binding.ivRecorrente.setOnClickListener {
            binding.edtDataLimiteRepetir.filters = arrayOf(InputFilter.LengthFilter(indeterminadamente.length))

            binding.edtDataLimiteRepetir.setText(indeterminadamente)
            binding.edtDataLimiteRepetir.clearFocus()
        }

        binding.edtDataLimiteRepetir.addTextChangedListener {


            dataLimiteDaRepeticao = if (indeterminadamente == it.toString()) {
                LIMITE_RECORRENCIA_INDEFINIDO
            } else it.toString().converterMMAAAAparaMillis()  // o valor setado será null até que seja digitada uma data valida

            if (it.toString().length <= compMaximMascara) binding.edtDataLimiteRepetir.filters = arrayOf(InputFilter.LengthFilter(compMaximMascara))
            atualizarDica()
        }

        binding.edtDataLimiteRepetir.addTextChangedListener(MascaraData.mascaraDataMeseAno())

    }

    private fun naoRepetir() {
        callback.concluido(null, null, null, fragmento.getString(R.string.Nao_repetir))
        dialogo.dismiss()
    }

    private fun validarEntradasUsuarioeFechar() {

        val tipoValido = validarTipoRepeticao()
        if (!tipoValido) return

        val qtdValida = validarQtdRepeticoes()
        if (!qtdValida) return

        val dataLimiteValida = validarDataLimite()
        if (!dataLimiteValida) return



        callback.concluido(intervaloDasRepeticoes, tipoDeRecorrencia, dataLimiteDaRepeticao!!, binding.tvDica.text.toString())
        dialogo.dismiss()
    }

    /**
     * Verifica se o usuario selecionou uma das opçoes de recorrencia da interface e notifica se
     * nao tiver feito
     * @return true se a entrada for valida
     */
    private fun validarTipoRepeticao(): Boolean {
        // TODO: precisa dessa funçao?
        //return if (binding.toggleButton.checkedButtonId == View.NO_ID) {
        //  fragmento.notificarErro(binding.toggleButton, fragmento.getString(R.string.Selecione_dias_ou_meses_para_prosseguir))
        //false
        //} else true

        return true
    }

    /**
     * verifica se a entrada do usuario nao é nula, vazia ou maior que o permitido
     * @return true se a entrada for valida
     */
    private fun validarQtdRepeticoes(): Boolean {

        if (binding.edtRepetir.text.isNullOrEmpty() || binding.edtRepetir.text.toString().toInt() < 1) {
            fragmento.notificarErro(binding.edtRepetir, fragmento.getString(R.string.Entrada_invalida))
            return false
        }

        return when (tipoDeRecorrencia) {
            MES  -> validarQtdRepeticoesMes()
            DIA  -> validarQtdRepeticoesDia()
            null -> false/*Só vai chegar nessa função se o usuario  selecionou um tipo de recorrencia*/
        }
    }

    private fun validarDataLimite(): Boolean {
        if (dataLimiteDaRepeticao == null) {
            fragmento.notificarErro(binding.edtRepetir, fragmento.getString(R.string.Selecione_a_data_limite_da_recorr_ncia))
            binding.edtDataLimiteRepetir.requestFocus()
        }
        return dataLimiteDaRepeticao != null
    }

    private fun validarQtdRepeticoesMes(): Boolean {
        if (intervaloDasRepeticoes > INTERVALO_MAX_REPETICAO_MESES) {
            fragmento.notificarErro(binding.edtRepetir, String.format(fragmento.getString(R.string.Intervalo_entre_meses_nao_pode_ser_maior_que_x), INTERVALO_MAX_REPETICAO_MESES))
            return false
        }
        return true
    }

    private fun validarQtdRepeticoesDia(): Boolean {

        if (intervaloDasRepeticoes > INTERVALO_MAX_REPETICAO_DIAS) {
            fragmento.notificarErro(binding.edtRepetir, String.format(fragmento.getString(R.string.Intervalo_entre_dias_nao_pode_ser_maior_que_x), INTERVALO_MAX_REPETICAO_DIAS))
            return false
        } else if (intervaloDasRepeticoes < INTERVALO_MIN_REPETICAO_DIAS) {
            fragmento.notificarErro(binding.edtRepetir, String.format(fragmento.getString(R.string.Intervalo_entre_dias_nao_pode_ser_menor_que_x), INTERVALO_MIN_REPETICAO_DIAS))
            return false
        }

        return true
    }

    fun mostrar() {/*se esse dialogo for cancelavel sera necessario definir um dismiss listener para
        * tirar o foco a view de repetir, senao ocorrera um bug toda vez que o usuario fechar
        * o dialogo sem ser pelos botoes(salvar e cancelar) onde é possivel editar o texto da view livremente
        * */
        dialogo.customView(binding.root).cancelavel(false).show(fragmento.parentFragmentManager)
    }

    fun interface Callback {
        fun concluido(intervaloDasRepeticoes: Int?, tipoDeRecorrencia: Recorrencia.Tipo?, dataLimite: Long?, dica: String)
    }
}
