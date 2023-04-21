package gmarques.debtv4.presenter.pop_ups

import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import gmarques.debtv4.domain.entidades.Despesa
import gmarques.debtv4.domain.extension_functions.Datas
import org.joda.time.DateTime

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

            callback.dataEscolhida(Datas.ajustarDataDoPicker(dataEmUTC))
        }

        picker.show(parentFragmentManager, "tag");
    }

    private fun criarLimites(): CalendarConstraints {

        val max = DateTime.now().plusYears(Despesa.VARIACAO_MAXIMA_DATA).millis
        val min = DateTime.now().minusYears(Despesa.VARIACAO_MAXIMA_DATA).millis

        return CalendarConstraints.Builder()
            .setStart(min)
            .setEnd(max)
            .build()
    }

    fun interface DataPickerCallback {
        fun dataEscolhida(dataEmUTC: Long)
    }
}