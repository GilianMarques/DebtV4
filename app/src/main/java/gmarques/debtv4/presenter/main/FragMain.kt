package gmarques.debtv4.presenter.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import gmarques.debtv4.R
import gmarques.debtv4.databinding.FragMainBinding
import gmarques.debtv4.domain.PeriodosController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FragMain : CustomFrag() {


    private val viewModel: FragMainViewModel by viewModels()
    private lateinit var binding: FragMainBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragMainBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (viewModel.ocultarSplashScreen) ocultarSplashScreen()
        verificarUsuarioLogado()
        Log.d("USUK", "FragMain.onViewCreated: ")
    }

    /**
     * Oculta de mandeira imediata a SplashView
     */
    private fun ocultarSplashScreen() {
        binding.splash.splashContainer.visibility = GONE
        viewModel.ocultarSplashScreen = true
    }

    /**
     * só carrego os dados depois de verificar que o usuario esta logado
     * */
    private fun verificarUsuarioLogado() {
        if (viewModel.usuarioLogado()) init()
        else findNavController().navigate(FragMainDirections.actionLogin())
    }

    private fun init() {
        initToolbar(binding, getString(R.string.Nova_despesa))
        binding.btnVerDespesas.setOnClickListener {
            findNavController().navigate(FragMainDirections.actionVerDespesas())

        }
        binding.btnAddDespesas.setOnClickListener {
            findNavController().navigate(FragMainDirections.actionAdicionarDespesa())

        }

        binding.btnSincronizar.setOnClickListener {
            viewModel.sincronizar()
        }

        lifecycleScope.launch {
            // delay(1000)
            ocultarSplashScreen()
        }


    }


}