package oscar.platoscore.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import oscar.platoscore.databinding.ItemResultadoBinding
import oscar.platoscore.models.Resultado

class ResultadoAdapter :
    ListAdapter<Resultado, ResultadoAdapter.ResultadoViewHolder>(DiffCallback()) {

    inner class ResultadoViewHolder(private val binding: ItemResultadoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Resultado) {
            val posicion = absoluteAdapterPosition + 1
            binding.tvPosicion.text = "$posicion."
            binding.tvNombre.text = item.tirador.nombreApellidos
            binding.tvPlatos.text = "${item.tirador.platosRotos} platos"
            binding.tvPrecio.text = "${"%.2f".format(item.precio)}€"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultadoViewHolder {
        val binding = ItemResultadoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ResultadoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ResultadoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Resultado>() {
        override fun areItemsTheSame(oldItem: Resultado, newItem: Resultado): Boolean =
            oldItem.tirador.id == newItem.tirador.id

        override fun areContentsTheSame(oldItem: Resultado, newItem: Resultado): Boolean =
            oldItem == newItem
    }
}
