package oscar.platoscore.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import oscar.platoscore.R
import oscar.platoscore.databinding.ItemEscuadraBinding
import oscar.platoscore.models.Escuadra
import oscar.platoscore.models.EscuadraConContadores

class EscuadraAdapter(
    private val onClickListener: (Escuadra) -> Unit,
    private val onLongClickListener: (Escuadra) -> Unit
) : ListAdapter<EscuadraConContadores, EscuadraAdapter.EscuadraViewHolder>(DiffCallback()) {

    inner class EscuadraViewHolder(private val binding: ItemEscuadraBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: EscuadraConContadores) {
            val contexto = binding.root.context
            val escuadra = item.escuadra
            binding.tvNumeroEscuadra.text =
                contexto.getString(R.string.item_numero_escuadra, escuadra.numeroEscuadra)
            binding.tvInfoEscuadra.text =
                contexto.getString(R.string.item_contador_tiradores, item.numTiradores)
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

    class DiffCallback : DiffUtil.ItemCallback<EscuadraConContadores>() {
        override fun areItemsTheSame(
            oldItem: EscuadraConContadores,
            newItem: EscuadraConContadores
        ): Boolean = oldItem.escuadra.id == newItem.escuadra.id

        override fun areContentsTheSame(
            oldItem: EscuadraConContadores,
            newItem: EscuadraConContadores
        ): Boolean = oldItem == newItem
    }
}
