package gmarques.debtv4.presenter.main

import android.content.Context
import android.os.Build
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import gmarques.debtv4.App
import gmarques.debtv4.R

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



}
