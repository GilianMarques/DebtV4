package gmarques.debtv4.presenter.pop_ups

import android.util.Log
import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import gmarques.debtv4.domain.entidades.DespesaRecorrente
import gmarques.debtv4.domain.extension_functions.Datas
import org.joda.time.DateTime
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * @Author: Gilian Marques
 * @Date: sexta-feira, 21 de abril de 2023 às 19:16
 */
class DataPicker(dataInicial: Long, parentFragmentManager: FragmentManager, callback: DataPickerCallback) {

    init {

        val picker = MaterialDatePicker.Builder.datePicker()
            .setSelection(dataInicial)
            .setTitleText("")
            .setCalendarConstraints(criarLimites())
            .build()

        picker.addOnPositiveButtonClickListener { dataEmUTC ->

            val dataFormatada = formatarString(dataEmUTC)
            callback.dataEscolhida(dataEmUTC, dataFormatada)

            Log.d("USUK", "DataPicker.$dataEmUTC, $dataFormatada: ")
        }

        picker.show(parentFragmentManager, "tag");
    }

    /**
     * converte o long para dd/mm/aaaa
     */
    private fun formatarString(dataEmUTC: Long): String {
        val dataFormat = SimpleDateFormat(Datas.Mascaras.DD_MM_AAAA.tipo, Locale.getDefault())
        return dataFormat.format(Datas.aplicarOffset(dataEmUTC))
    }

    private fun criarLimites(): CalendarConstraints {

        val max = DateTime.now().plusYears(DespesaRecorrente.DATA_LIMITE_IMPORATACAO).millis
        val min = DateTime.now().minusYears(DespesaRecorrente.DATA_LIMITE_IMPORATACAO).millis

        return CalendarConstraints.Builder()
            .setStart(min)
            .setEnd(max)
            .build()
    }

    fun interface DataPickerCallback {
        fun dataEscolhida(dataEmUTC: Long, dataFormatada: String)
    }
}