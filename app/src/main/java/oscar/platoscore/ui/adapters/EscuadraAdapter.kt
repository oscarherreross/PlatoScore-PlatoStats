package oscar.platoscore.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import oscar.platoscore.databinding.ItemEscuadraBinding
import oscar.platoscore.models.Escuadra

class EscuadraAdapter(
    private val onClickListener: (Escuadra) -> Unit,
    private val onLongClickListener: (Escuadra) -> Unit
) : ListAdapter<Escuadra, EscuadraAdapter.EscuadraViewHolder>(DiffCallback()) {

    inner class EscuadraViewHolder(private val binding: ItemEscuadraBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(escuadra: Escuadra) {
            binding.tvNumeroEscuadra.text = "Escuadra ${escuadra.numeroEscuadra}"
            binding.root.setOnClickListener {
                onClickListener(escuadra)
            }
            binding.root.setOnLongClickListener {
                onLongClickListener(escuadra)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EscuadraViewHolder {
        val binding = ItemEscuadraBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EscuadraViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EscuadraViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Escuadra>() {
        override fun areItemsTheSame(oldItem: Escuadra, newItem: Escuadra): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Escuadra, newItem: Escuadra): Boolean =
            oldItem == newItem
    }
}
