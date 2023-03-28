package gmarques.debtv4.presenter.outros

import android.view.View
import android.view.animation.Animation
import android.view.animation.ScaleAnimation

/**
 * Criado por Gilian Marques
 * Sábado, 20 de Julho de 2019  as 18:08:03.
 */
abstract class AnimatedClickListener : View.OnClickListener {
    override fun onClick(view: View) {
        val anim: Animation = ScaleAnimation(
            1f, 1.018f,
            1f, 1.018f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        anim.fillAfter = true // Needed to keep the result of the animation
        anim.duration = 150
        anim.repeatCount = 1
        anim.repeatMode = Animation.REVERSE
        view.startAnimation(anim)
    }
}