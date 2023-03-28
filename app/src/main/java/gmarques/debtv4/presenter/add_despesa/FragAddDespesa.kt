package gmarques.debtv4.presenter.add_despesa

import android.animation.ValueAnimator
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.animation.PathInterpolatorCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import gmarques.debtv4.R
import gmarques.debtv4.databinding.FragAddDespesaBinding
import gmarques.debtv4.domain.extension_functions.ExtFunctions.Companion.porcentoDe
import gmarques.debtv4.presenter.main.CustomFrag
import gmarques.debtv4.presenter.outros.UIUtils
import kotlin.math.abs

class FragAddDespesa : CustomFrag() {


    private lateinit var viewModel: FragAddDespesaViewModel
    private lateinit var binding: FragAddDespesaBinding
    private lateinit var cornerAnimation: ValueAnimator
    private lateinit var statusBarColorAnimatior: ValueAnimator


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragAddDespesaBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[FragAddDespesaViewModel::class.java]
        initToolbar(binding,getString(R.string.Nova_despesa))
        initAnimacaoDosCantosDoScrollView()
        initAnimacaoDeCorDaStatusBar()
        initAppBar()
    }

    private fun initAppBar() {
        val anims = arrayListOf(cornerAnimation, statusBarColorAnimatior)

        binding.appbar.post {
            val maxScroll: Int = binding.appbar.measuredHeight
            binding.appbar.addOnOffsetChangedListener { _, vertOffset ->

                val porcentagem = abs(vertOffset).porcentoDe(maxScroll)
                anims.forEach {
                    it.setCurrentFraction(porcentagem / 100)
                }
            }
        }
    }

    private fun initAnimacaoDeCorDaStatusBar() {
        val decorView: View = requireActivity().window.decorView
        val decorViewFlags = decorView.systemUiVisibility

        val targetColor: Int = UIUtils.corAttr(android.R.attr.windowBackground, requireActivity())

        statusBarColorAnimatior =
                ValueAnimator.ofArgb(requireActivity().window.statusBarColor, targetColor)
        statusBarColorAnimatior.interpolator =
                PathInterpolatorCompat.create(1.000f, 0.000f, 1.000f, 1.030f)
        statusBarColorAnimatior.addUpdateListener { animation ->
            requireActivity().window.statusBarColor = animation.animatedValue as Int

            if (animation.animatedFraction < 0.80) {
                decorView.systemUiVisibility = decorViewFlags
            } else {
                decorView.systemUiVisibility =
                        decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

    private fun initAnimacaoDosCantosDoScrollView() {
        val drawable = binding.nestedScroll.background as GradientDrawable
        // pego o raio X do canto superior esquerdo e uso como parametro para os raios da
        //parte superior do drawable
        val raioEmDp = drawable.cornerRadii?.get(1) ?: 0f

        cornerAnimation = ValueAnimator.ofFloat(raioEmDp, 0f)
        cornerAnimation.interpolator = PathInterpolatorCompat.create(1.000f, 0.000f, 1.000f, 1.030f)

        cornerAnimation.addUpdateListener { animation ->
            val cornerRadius = animation.animatedValue as Float
            drawable.cornerRadii = floatArrayOf(
                cornerRadius, cornerRadius, //top left
                cornerRadius, cornerRadius, // top right
                0f, 0f, // bottom right
                0f, 0f // bottom left
            )
        }

    }


}