package oscar.platoscore.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import oscar.platoscore.databinding.ItemTiradaBinding
import oscar.platoscore.models.Tirada
import oscar.platoscore.models.TiradaConContadores
import oscar.platoscore.utils.Fechas

class TiradaAdapter(
    private val onClickListener: (Tirada) -> Unit,
    private val onLongClickListener: (Tirada) -> Unit
) : ListAdapter<TiradaConContadores, TiradaAdapter.TiradaViewHolder>(DiffCallback()) {

    inner class TiradaViewHolder(private val binding: ItemTiradaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TiradaConContadores) {
            val tirada = item.tirada
            binding.tvNombreTirada.text = tirada.nombre
            binding.tvFechaTirada.text = "Fecha: ${Fechas.mostrar(tirada.fecha)}"
            binding.tvInfoTirada.text =
                "Escuadras: ${item.numEscuadras} · Tiradores: ${item.numTiradores}"
            binding.root.setOnClickListener {
                onClickListener(tirada)
            }
            binding.root.setOnLongClickListener {
                onLongClickListener(tirada)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TiradaViewHolder {
        val binding = ItemTiradaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TiradaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TiradaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<TiradaConContadores>() {
        override fun areItemsTheSame(
            oldItem: TiradaConContadores,
            newItem: TiradaConContadores
        ): Boolean = oldItem.tirada.id == newItem.tirada.id

        override fun areContentsTheSame(
            oldItem: TiradaConContadores,
            newItem: TiradaConContadores
        ): Boolean = oldItem == newItem
    }
}
