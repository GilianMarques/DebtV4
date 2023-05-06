package gmarques.debtv4.presenter.ver_despesas

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import dagger.hilt.android.AndroidEntryPoint
import gmarques.debtv4.R
import gmarques.debtv4.databinding.FragVerDespesasBinding
import gmarques.debtv4.domain.entidades.Despesa
import gmarques.debtv4.presenter.main.CustomFrag
import gmarques.debtv4.presenter.ver_despesas.adapter.DespesasAdapter
import gmarques.debtv4.presenter.ver_despesas.adapter.DespesasAdapterCallback
import kotlinx.coroutines.launch


@AndroidEntryPoint
class FragVerDespesas : CustomFrag(), DespesasAdapterCallback {

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
        initToolbar(binding,getString(R.string.Ver_despesas))
        init()

    }

    private fun init() {

        initRecyclerView()
    }

    private fun initRecyclerView() {

        val layoutManager = FlexboxLayoutManager(requireContext())
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.SPACE_AROUND

        val adapter = DespesasAdapter(this@FragVerDespesas, this@FragVerDespesas)
        binding.rvDespesas.setHasFixedSize(true)
        binding.rvDespesas.adapter = adapter
        binding.rvDespesas.layoutManager = layoutManager


        lifecycleScope.launch {
            viewModel.carregarDespesas().collect {
                adapter.atualizarColecao(it)
                Log.d("USUK", "FragVerDespesas.initRecyclerView: atualizado ${it.size}")
            }
        }


    }

    override fun mostrarBottomSheetResumo(despesa: Despesa) {
// TODO: implementar
    }


}




