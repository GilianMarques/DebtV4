package gmarques.debtv4.presenter.outros

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.os.*
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import gmarques.debtv4.App
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * Criado por Gilian Marques (Java no projeto DebtV3)
 * Sexta-feira, 19 de Julho de 2019  as 23:22:35.
 * Reformulado e reutilizado em 26/03/2023 14:37
 */
object UIUtils {

    enum class Vibracao() {
        INTERACAO, ERRO, SUCESSO
    }

    /**
     * Função responsável por gerar uma vibração no dispositivo. A intensidade e a duração da
     * vibração são definidas de acordo com o tipo de vibração passado como parâmetro.
     * Para dispositivos com Android 12 ou superior, é utilizado o VibratorManager para gerenciar as vibrações.
     * Caso contrário, é utilizado o Vibrator padrão do dispositivo.
     *@param tipo o tipo de vibração a ser executada (INTERACAO, SUCESSO ou ERRO)
     */
    fun vibrar(tipo: Vibracao) {
        val vibrator = ContextCompat.getSystemService(App.inst, Vibrator::class.java) as Vibrator
        when (tipo) {
            Vibracao.INTERACAO -> vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            Vibracao.SUCESSO   -> vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(25, 25, 25, 25, 25, 25), VibrationEffect.DEFAULT_AMPLITUDE))
            Vibracao.ERRO      -> vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    fun corAttr(reference: Int, activity: Activity): Int {
        val typedValue = TypedValue()
        val theme: Resources.Theme = activity.theme
        theme.resolveAttribute(reference, typedValue, true)
        return typedValue.data
    }

    fun cor(reference: Int): Int {
        return ContextCompat.getColor(App.inst, reference)
    }

    @ColorInt
    fun corComTransaparencia(cor_: Int, factor: Float, activity: Activity): Int {
        var cor = cor_
        cor = corAttr(cor, activity)
        cor = mudarTransaprencia(cor, factor)
        return cor
    }

    /**
     * @param color  c
     * @param factor the higher the transparent color will be (0.9 almost invisible)
     * @return x
     */
    @ColorInt
    fun mudarTransaprencia(color: Int, factor: Float): Int {
        val alpha = (Color.alpha(color) * factor).roundToInt()
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(1 - alpha, red, green, blue)
    }

    fun mudarCor(btnPaydOut: TextView, from: Int, to: Int) {
        val animator: ValueAnimator = ValueAnimator.ofArgb(from, to)
        animator.duration = 200
        animator.addUpdateListener { valueAnimator -> btnPaydOut.setTextColor(valueAnimator.animatedValue as Int) }
        animator.start()
    }


    fun aplicarTema(image: Drawable?, cor: Int): Drawable? {
        if (image == null) return null
        image.mutate()
        val porterDuffColorFilter = PorterDuffColorFilter(cor, PorterDuff.Mode.SRC_ATOP)
        image.colorFilter = porterDuffColorFilter
        return image
    }

    fun aplicarTema(@DrawableRes image: Int, @ColorInt cor: Int): Drawable {
        val draw: Drawable = ContextCompat.getDrawable(App.inst, image)!!
        return aplicarTema(draw, cor)!!
    }

    fun mostrarTeclado(view: View) {
        view.post {

            val motionEventDown: MotionEvent = MotionEvent.obtain(SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis() + 100, MotionEvent.ACTION_DOWN, view.measuredWidth.toFloat(),
                (view.measuredHeight / 2).toFloat(), 0)

            val motionEventUP: MotionEvent = MotionEvent.obtain(SystemClock.uptimeMillis() + 200,
                SystemClock.uptimeMillis() + 300, MotionEvent.ACTION_UP, view.measuredWidth.toFloat(),
                (view.measuredHeight / 2).toFloat(), 0)

            view.dispatchTouchEvent(motionEventDown)
            motionEventDown.recycle()

            view.dispatchTouchEvent(motionEventUP)
            motionEventUP.recycle()
        }
    }

    fun ocultarTeclado(view: View) {
        val imm = App.inst.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    /**
     * @param color  to manipulate
     * @param factor 1.0f nothing changes <1.0f darker >1.0f lighter
     * @return manipulated color
     */
    fun manipularCor(color: Int, factor: Float): Int {
        val a = Color.alpha(color)
        val r = (Color.red(color) * factor).roundToInt()
        val g = (Color.green(color) * factor).roundToInt()
        val b = (Color.blue(color) * factor).roundToInt()
        return Color.argb(a, r.coerceAtMost(255), g.coerceAtMost(255), b.coerceAtMost(255))
    }

    /**
     * @return o tamanho da tela em pixels width e height
     */
    fun tamanhoTela(): Pair<Int, Int> {

        val windowManager = App.inst.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            val rect = windowMetrics.bounds
            rect.width() to rect.height()
        } else {
            App.inst.resources.displayMetrics.widthPixels to App.inst.resources.displayMetrics.heightPixels
        }
    }


    /**
     *
     * @param activityContext Deve ser o context de uma activity para que o calculo seja feito
     * de maneira correta mesmo em casos de dispositivos rodando apps em multi-janela
     *
     * @return o tamanho da tela descontando as areas da statusbar e navbar
     */

    fun tamanhoTelaSemStatusOuNavBar(activityContext: Context): Pair<Int, Int> {


        val windowManager = activityContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // TODO:  testar em android oreo
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            val windowMetrics = windowManager.currentWindowMetrics
            val barrasDoSistema = windowManager.currentWindowMetrics.windowInsets.getInsets(WindowInsets.Type.systemBars())

            var alturaTela = windowMetrics.bounds.height()
            val larguraTela = windowMetrics.bounds.width()

            alturaTela -= barrasDoSistema.top
            alturaTela -= barrasDoSistema.bottom

            larguraTela to alturaTela
        } else {
            @Suppress("DEPRECATION")
            val display: Display = windowManager.defaultDisplay
            val size = Point()
            @Suppress("DEPRECATION")
            display.getSize(size)
            size.x to size.y
        }
    }


}