package gmarques.debtv4.presenter.ver_despesas

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import gmarques.debtv4.R
import gmarques.debtv4.databinding.FragVerDespesasBinding
import gmarques.debtv4.domain.MesesController
import gmarques.debtv4.domain.entidades.Despesa
import gmarques.debtv4.presenter.main.CustomFrag
import gmarques.debtv4.presenter.ver_despesas.adapter.DespesasAdapter
import gmarques.debtv4.presenter.ver_despesas.adapter.DespesasAdapterCallback
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.joda.time.DateTime


@AndroidEntryPoint
class FragVerDespesas : CustomFrag(), DespesasAdapterCallback {

    private lateinit var adapter: DespesasAdapter

    // para injetar com hilt ao inves de usar @Inject. É assim que se injeta viewModels
    private val viewModel: FragVerDespesasViewModel by viewModels()

    private lateinit var binding: FragVerDespesasBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragVerDespesasBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar(binding, getString(R.string.Ver_despesas))
        init()

    }

    private fun init() {

        initRecyclerView()
        observarPeriodo()
        lifecycleScope.launch {

            delay(3000)
            MesesController.proximoMes()

            delay(3000)
            MesesController.mesAnterior()
            MesesController.mesAnterior()

            delay(3000)
            MesesController.selecionarPeriodo(0, DateTime.now().plusYears(1).millis)

            delay(3000)
            MesesController.mesAtual()

        }
    }

    // TODO: lsiteners duplicados sao disparados quando atualizo db     
    /**
     * Observa as alterações no período atual e atualiza a lista de despesas com os novos valores.
     */
    private fun observarPeriodo() {
        MesesController.periodoAtual.observe(viewLifecycleOwner) { periodo ->
            lifecycleScope.launch {
                viewModel.carregarDespesas(periodo.inicio, periodo.fim).collect {
                    adapter.atualizarColecao(it)
                    Log.d("USUK", "FragVerDespesas.observarPeriodo: ouvindo ${periodo.nome}")
                }
            }
        }
    }

    private fun initRecyclerView() {

        adapter = DespesasAdapter(this@FragVerDespesas, this@FragVerDespesas)

        val layoutManager = FlexboxLayoutManager(requireContext())
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.SPACE_AROUND

        binding.rvDespesas.setHasFixedSize(true)
        binding.rvDespesas.adapter = adapter
        binding.rvDespesas.layoutManager = layoutManager

    }

    override fun mostrarBottomSheetResumo(despesa: Despesa) {
// TODO: implementar
    }


}




