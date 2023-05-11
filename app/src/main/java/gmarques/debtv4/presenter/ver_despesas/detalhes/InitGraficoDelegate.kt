package gmarques.debtv4.presenter.ver_despesas.detalhes

import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import gmarques.debtv4.App
import gmarques.debtv4.R
import gmarques.debtv4.domain.entidades.Despesa
import gmarques.debtv4.domain.extension_functions.Datas
import gmarques.debtv4.domain.extension_functions.Datas.Companion.dataFormatada
import gmarques.debtv4.domain.extension_functions.ExtensionFunctions.Companion.dp
import gmarques.debtv4.domain.extension_functions.ExtensionFunctions.Companion.emMoeda
import gmarques.debtv4.domain.usecases.ObservarDespesasUseCase
import gmarques.debtv4.presenter.outros.UIUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import javax.inject.Inject

/**
 * Criado por Gilian Marques
 * Domingo, 28 de Julho de 2019  as 19:36:26.
 */
class InitGraficoDelegate @Inject constructor(
    private val despesasUsecase: ObservarDespesasUseCase,
) {

    lateinit var lineChart: LineChart
    lateinit var despesa: Despesa
    lateinit var activity: FragmentActivity

    private var primary: Int = 0
    private var accent: Int = 0
    private var secondary: Int = 0
    lateinit var job: Job

    private fun initVariaveis() {
        lineChart.visibility = View.GONE
        primary = UIUtils.corAttr(android.R.attr.colorPrimary, activity)
        accent = UIUtils.corAttr(android.R.attr.colorAccent, activity)
        secondary = UIUtils.cor(R.color.color_secondary)
    }

    fun initGrafico() {
        initVariaveis()
        carregarDados()
    }

    private fun carregarDados() = apply {

        job = activity.lifecycleScope.launch {
            despesasUsecase( DateTime(DateTimeZone.UTC).minusMonths(3).millis, DateTime(DateTimeZone.UTC).plusMonths(3).millis).collect { despesas ->
                val dados = ArrayList<Entry>()

                for (despesa in despesas) {
                    dados.add(Entry(despesa.dataDoPagamento.toFloat(), despesa.valor.toFloat())) // TODO: filtrar despesa
                }
                //  job.cancel()
                atualizarGrafico(dados)
            }
        }


    }

    private fun atualizarGrafico(dados: ArrayList<Entry>) {
        val dataSet = carregarDataSet(dados)

        //-------------------------------------------------------------------
        // create a data object with the data sets
        val data = LineData(dataSet)
        data.setValueTextColor(accent)
        data.setValueTextSize(9f)

        //customize -------------------------------------------------------------
        val xAxis: XAxis = lineChart.xAxis
        xAxis.setDrawAxisLine(false)
        xAxis.setDrawGridLines(true)
        xAxis.setDrawLimitLinesBehindData(false)
        xAxis.setDrawGridLinesBehindData(false)
        xAxis.position = XAxis.XAxisPosition.BOTTOM_INSIDE
        xAxis.yOffset = 5f
        xAxis.textColor = accent
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase): String {
                val date: String = value.toLong().dataFormatada(Datas.Mascaras.DD_MM_AAAA)
                return date.substring(2) //remove the day from string. '22 AGO' becomes 'AGO'
            }
        }
        //-----------------------------------------------------------------------
        val leftYAxis: YAxis = lineChart.axisLeft
        leftYAxis.isEnabled = false
        val rightYAxis: YAxis = lineChart.axisRight
        rightYAxis.isEnabled = false

        // xAxis.setEnabled(false);
        val description = Description()
        description.text = ""
        lineChart.description = description

        // dismiss legend (that colored square at bottom of chart)
        val legend: Legend = lineChart.legend
        legend.isEnabled = false
        val offset: Float = 10.dp().toFloat()
        lineChart.setViewPortOffsets(offset, offset * 2, offset, offset)


        // lineChart.setAutoScaleMinMaxEnabled(true);
        // avoid repeated x values
        lineChart.axisLeft.isGranularityEnabled = false
        lineChart.axisLeft.granularity = 1f
        lineChart.axisLeft.setLabelCount(dados!!.size, true)
        lineChart.isDoubleTapToZoomEnabled = false
        lineChart.isHighlightPerDragEnabled = true
        lineChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry, h: Highlight) {
                Log.d("USUK", "GraficoDeLinhaComInfo.onValueSelected: ${e.y}")
            }

            override fun onNothingSelected() {}
        })


        // set data
        lineChart.setData(data)
        lineChart.invalidate()
        lineChart.setVisibility(View.VISIBLE)
        lineChart.animateY(650, Easing.EaseInOutExpo)
        lineChart.fitScreen()
    }

    /**
     * Aqui se defini o degrade do grafico, cor, tamanho e formato da linha, circulos e formato do
     * texto que aparece sobre cada circulo do grafico
     */
    private fun carregarDataSet(dados: ArrayList<Entry>): LineDataSet {
        val dataSet = LineDataSet(dados, "")
// TODO: terminar de refatorar grafico
        dataSet.axisDependency = YAxis.AxisDependency.LEFT

        dataSet.color = accent /* cor da linha*/
        dataSet.circleColors = arrayListOf(accent)
        dataSet.circleHoleColor = secondary
        dataSet.highLightColor = accent /*cor da marca que aparece quando seleciona uma valor do grafico*/

        dataSet.lineWidth = 0.8f.dp()
        dataSet.circleHoleRadius = 0.5f.dp()
        dataSet.circleRadius = 1.5f.dp()

        dataSet.fillDrawable = ContextCompat.getDrawable(App.inst, R.drawable.back_gradiente_grafico_linha);

        dataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER/*suavização da linha do grafico*/

        dataSet.setDrawCircleHole(true)
        dataSet.setDrawFilled(true)
        dataSet.setDrawHorizontalHighlightIndicator(true)

        dataSet.isVisible = true

        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return ""//value.toString().emMoeda()
            }
        }

        return dataSet
    }
}
