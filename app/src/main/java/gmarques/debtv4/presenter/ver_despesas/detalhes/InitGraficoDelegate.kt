package gmarques.debtv4.presenter.ver_despesas.detalhes

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
import gmarques.debtv4.domain.extension_functions.Datas.Companion.finalDoMes
import gmarques.debtv4.domain.extension_functions.ExtensionFunctions.Companion.dp
import gmarques.debtv4.domain.extension_functions.ExtensionFunctions.Companion.emMoeda
import gmarques.debtv4.domain.usecases.despesas.ObservarDespesasPorNomeNoPeriodoUseCase
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
    private val despesasUsecase: ObservarDespesasPorNomeNoPeriodoUseCase,
) {

    lateinit var clickListener: (Despesa) -> Unit
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

    fun executar() {
        initVariaveis()
        carregarDados()
    }

    private fun carregarDados() = apply {

        job = activity.lifecycleScope.launch {
            despesasUsecase(despesa.nome,
                DateTime(DateTimeZone.UTC).minusMonths(6).finalDoMes().millis,
                DateTime(DateTimeZone.UTC).plusMonths(6).finalDoMes().millis)
                .collect { despesas ->

                    val dados = ArrayList<Entry>()
                    despesas.forEach { despesa ->
                        dados.add(
                            Entry(despesa.dataDoPagamento.toFloat(),
                                despesa.valor.toFloat())
                                .apply { data = despesa })
                    }
                    atualizarGrafico(dados)
                }
        }


    }

    /**
     * Executa as funçoes necessarias na ordem correta pra iniciar o grafico
     */
    private fun atualizarGrafico(dados: ArrayList<Entry>) {

        val dataSet = carregarDataSet(dados)
        val lineData = carregarLineData(dataSet)

        initEixoX()
        initEixoY(dataSet)
        initDescricao()
        initLegenda()
        initGrafico(lineData)


    }

    /**
     * Aplica as costumizaçoes pertinentes ao grafico em si
     */
    private fun initGrafico(lineData: LineData) {

        val comprimentoTexto = lineData.yMax.toString().emMoeda().length

        /*'comprimentoTexto * 6.5f.dp()' deixa um espaço perfeito entre a borda esquerda do grafico e
        os valores (em moeda) se o comprimento for '7', por isso
        * se o comprimento for maior que 7 eu subtraio para obter a sobra e multiplico por um numero
        * que resulta em um resultado aceitavel que nesse caso é 1.75f*/
        val offsetVariavel: Float = comprimentoTexto * 6.5f.dp() - ((comprimentoTexto - 7) * 1.75f.dp())
        val offset = 30f.dp()

        // aplica margens no grafico
        lineChart.setViewPortOffsets(offsetVariavel, offset / 2, offset / 2, offset / 2)


        lineChart.isAutoScaleMinMaxEnabled = true

        // avoid repeated x values
        lineChart.isDoubleTapToZoomEnabled = false
        lineChart.isHighlightPerDragEnabled = true
        lineChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry, h: Highlight) {
                clickListener(e.data as Despesa)
            }

            override fun onNothingSelected() {
            }
        })

        lineChart.setNoDataText(activity.getString(R.string.Essa_despesa_nao_se_repete))  // Texto a ser exibido quando não há dados
        lineChart.setNoDataTextColor(primary)

        // set data
        if (lineData.entryCount > 1) lineChart.data = lineData
        lineChart.invalidate()
        lineChart.visibility = View.VISIBLE
        lineChart.animateY(650, Easing.EaseInOutExpo)
        lineChart.fitScreen()
    }

    /**
     * atualmente, oculta a legenda que é um conjunto de quadradinhos com os nomes dos dados
     *  nas cores que fica em baixo do grafico
     */
    private fun initLegenda() {

        // dismiss legend (that colored square at bottom of chart)
        val legend: Legend = lineChart.legend
        legend.isEnabled = false

    }

    /**
     * Atualmente oculta a descrição do grafico que fica no canto inferior direito
     */
    private fun initDescricao() {
        val description = Description()
        description.text = ""
        lineChart.description = description
    }

    /**
     * Inicializa o eixo Y do grafico, no caso, o esquerdo, o direito fica oculto
     *
     */
    private fun initEixoY(dataSet: LineDataSet) {
        val leftYAxis: YAxis = lineChart.axisLeft
        leftYAxis.isEnabled = true
        leftYAxis.axisMinimum = 0f
        leftYAxis.mAxisMaximum = dataSet.yMax
        //leftYAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
        //leftYAxis.addLimitLine(LimitLine(0.13f,"??"))
        leftYAxis.enableGridDashedLine(5f.dp(), 5f.dp(), 0f)
        leftYAxis.valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase): String {
                return value.toString().emMoeda()
            }
        }
        leftYAxis.setLabelCount(3, true)
        leftYAxis.textColor = accent
        // oculta o lado direito do eixo
        lineChart.axisRight.isEnabled = false
    }

    /**
     * inicializa o eixo X do grafico responsavel pelo nome dos meses
     * linhas e valores horizontais
     */
    private fun initEixoX() {
        val xAxis: XAxis = lineChart.xAxis
        xAxis.setDrawAxisLine(false)
        xAxis.setDrawGridLines(false)
        xAxis.setDrawLimitLinesBehindData(true)
        xAxis.setDrawGridLinesBehindData(false)
        xAxis.position = XAxis.XAxisPosition.BOTTOM_INSIDE // posicao do nome dos meses
        xAxis.yOffset = 1.0f.dp() // margem entre o nome dos meses e o bottom do grafico

        xAxis.textColor = accent
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase): String {
                return Datas.nomeDoMesAbreviado(value.toLong())
            }
        }
    }

    private fun carregarLineData(dataSet: LineDataSet): LineData {
        return LineData(dataSet)
    }

    /**
     * Aqui se define o gradiente do grafico, cor, tamanho e formato da linha, circulos e formato do
     * texto que aparece sobre cada circulo do grafico
     */
    private fun carregarDataSet(dados: ArrayList<Entry>): LineDataSet {
        val dataSet = LineDataSet(dados, "")

        dataSet.axisDependency = YAxis.AxisDependency.LEFT

        dataSet.color = accent /* cor da linha*/
        dataSet.circleColors = arrayListOf(accent)
        dataSet.circleHoleColor = secondary
        dataSet.highLightColor = accent /*cor da marca que aparece quando seleciona uma valor do grafico*/

        dataSet.lineWidth = 0.8f.dp()
        dataSet.circleHoleRadius = 0.5f.dp()
        dataSet.circleRadius = 1.5f.dp()

        dataSet.setDrawCircles(false)
        dataSet.fillDrawable = ContextCompat.getDrawable(App.inst, R.drawable.back_gradiente_grafico_linha);

        dataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER/*suavização da linha do grafico*/

        dataSet.setDrawCircleHole(true)
        dataSet.setDrawFilled(true)
        dataSet.setDrawHorizontalHighlightIndicator(true)
        dataSet.setDrawValues(false)

        dataSet.isVisible = true

        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return value.toString().emMoeda()
            }
        }

        return dataSet
    }
}
