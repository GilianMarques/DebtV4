package gmarques.debtv4.presenter.main

import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar
import gmarques.debtv4.R
import gmarques.debtv4.presenter.outros.UIUtils

open class CustomFrag : Fragment() {
    
    
    protected fun initToolbar(binding: ViewBinding, titulo: String) {
        val tvTitulo = binding.root.findViewById<TextView>(R.id.titulo)
        val menu = binding.root.findViewById<AppCompatImageView>(R.id.menu)
        val voltar = binding.root.findViewById<AppCompatImageView>(R.id.voltar)
        
        tvTitulo.text = titulo
        voltar.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }
    
    // TODO: documentar
    fun notificarErro(edtRepetir: View, mensagem: String) {
        Snackbar.make(requireContext(), edtRepetir, mensagem, Snackbar.LENGTH_LONG).show()
        UIUtils.vibrar(UIUtils.Vibracao.ERRO)
        view?.requestFocus()
    }
    
    
}
