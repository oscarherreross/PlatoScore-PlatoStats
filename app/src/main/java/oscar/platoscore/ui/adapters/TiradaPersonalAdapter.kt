package oscar.platoscore.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import oscar.platoscore.R
import oscar.platoscore.databinding.ItemTiradaPersonalBinding
import oscar.platoscore.models.TiradaPersonalConSeries
import oscar.platoscore.utils.Fechas

class TiradaPersonalAdapter(
    private val onClickListener: (TiradaPersonalConSeries) -> Unit,
    private val onLongClickListener: (TiradaPersonalConSeries) -> Unit
) : ListAdapter<TiradaPersonalConSeries, TiradaPersonalAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemTiradaPersonalBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TiradaPersonalConSeries) {
            val contexto = binding.root.context
            binding.tvLugar.text = item.tirada.lugar
            binding.tvFechaHora.text = Fechas.mostrarFechaHora(item.tirada.fechaHora)
            binding.tvEscuadraPuesto.text = contexto.getString(
                R.string.item_personal_escuadra_puesto,
                item.tirada.numeroEscuadra,
                item.tirada.puestoInicial
            )
            binding.tvResumen.text = contexto.getString(
                R.string.item_personal_resumen,
                item.platosRotos,
                item.platosPosibles,
                "%.1f".format(item.porcentaje)
            )
            binding.root.setOnClickListener { onClickListener(item) }
            binding.root.setOnLongClickListener {
                onLongClickListener(item)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTiradaPersonalBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<TiradaPersonalConSeries>() {
        override fun areItemsTheSame(
            oldItem: TiradaPersonalConSeries,
            newItem: TiradaPersonalConSeries
        ): Boolean = oldItem.tirada.id == newItem.tirada.id

        override fun areContentsTheSame(
            oldItem: TiradaPersonalConSeries,
            newItem: TiradaPersonalConSeries
        ): Boolean = oldItem == newItem
    }
}
