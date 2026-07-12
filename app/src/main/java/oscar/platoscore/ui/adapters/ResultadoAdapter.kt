package oscar.platoscore.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import oscar.platoscore.R
import oscar.platoscore.databinding.ItemResultadoBinding
import oscar.platoscore.models.Resultado

class ResultadoAdapter(
    private val onClickListener: (Resultado) -> Unit = {}
) : ListAdapter<Resultado, ResultadoAdapter.ResultadoViewHolder>(DiffCallback()) {

    inner class ResultadoViewHolder(private val binding: ItemResultadoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Resultado) {
            val contexto = binding.root.context
            binding.tvPosicion.text = if (item.empatado) "=${item.posicion}." else "${item.posicion}."
            binding.tvNombre.text = item.tirador.nombreApellidos
            binding.tvPlatos.text = contexto.getString(R.string.item_platos, item.tirador.platosRotos)
            binding.tvPrecio.text = contexto.getString(R.string.item_precio, "%.2f".format(item.precio))
            binding.root.setOnClickListener {
                onClickListener(item)
            }
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
