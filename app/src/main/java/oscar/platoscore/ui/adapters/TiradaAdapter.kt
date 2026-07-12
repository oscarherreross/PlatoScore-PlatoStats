package oscar.platoscore.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import oscar.platoscore.databinding.ItemTiradaBinding
import oscar.platoscore.models.Tirada

class TiradaAdapter(
    private val onClickListener: (Tirada) -> Unit,
    private val onLongClickListener: (Tirada) -> Unit
) : ListAdapter<Tirada, TiradaAdapter.TiradaViewHolder>(DiffCallback()) {

    inner class TiradaViewHolder(private val binding: ItemTiradaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(tirada: Tirada) {
            binding.tvNombreTirada.text = tirada.nombre
            binding.tvFechaTirada.text = "Fecha: ${tirada.fecha}"
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

    class DiffCallback : DiffUtil.ItemCallback<Tirada>() {
        override fun areItemsTheSame(oldItem: Tirada, newItem: Tirada): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Tirada, newItem: Tirada): Boolean =
            oldItem == newItem
    }
}
