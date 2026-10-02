package oscar.platostats.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import oscar.plato.core.utils.Fechas
import oscar.platostats.R
import oscar.platostats.databinding.ItemTiradaBinding
import oscar.platostats.models.Tirada
import oscar.platostats.models.TiradaConSeries

class TiradaAdapter(
    private val onClickListener: (TiradaConSeries) -> Unit,
    private val onLongClickListener: (TiradaConSeries) -> Unit
) : ListAdapter<TiradaConSeries, TiradaAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemTiradaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TiradaConSeries) {
            val contexto = binding.root.context
            binding.tvLugar.text = item.tirada.lugar
            binding.tvFechaHora.text = Fechas.mostrarFechaHora(item.tirada.fechaHora)
            val tipoLabel = contexto.getString(
                if (item.tirada.tipo == Tirada.TIPO_COMPETICION) R.string.tipo_competicion
                else R.string.tipo_entrenamiento
            )
            val maquinaLabel = contexto.getString(
                when (item.tirada.maquina) {
                    Tirada.MAQUINA_TRAP -> R.string.maquina_trap
                    Tirada.MAQUINA_OLIMPICO -> R.string.maquina_olimpico
                    else -> R.string.maquina_robot
                }
            )
            binding.tvEscuadraPuesto.text = contexto.getString(
                R.string.item_escuadra_tipo,
                item.tirada.numeroEscuadra,
                tipoLabel,
                maquinaLabel
            )
            binding.tvResumen.text = contexto.getString(
                R.string.item_resumen,
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
        val binding = ItemTiradaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<TiradaConSeries>() {
        override fun areItemsTheSame(
            oldItem: TiradaConSeries,
            newItem: TiradaConSeries
        ): Boolean = oldItem.tirada.id == newItem.tirada.id

        override fun areContentsTheSame(
            oldItem: TiradaConSeries,
            newItem: TiradaConSeries
        ): Boolean = oldItem == newItem
    }
}
