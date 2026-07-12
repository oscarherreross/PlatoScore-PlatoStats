package oscar.platoscore.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import oscar.platoscore.databinding.ItemTiradorBinding
import oscar.platoscore.models.Tirada
import oscar.platoscore.models.Tirador

class TiradorAdapter(private val onClickListener: (Tirador) -> Unit) :
    ListAdapter<Tirador, TiradorAdapter.TiradorViewHolder>(DiffCallback()) {

    /** Tirada a la que pertenecen los tiradores; necesaria para calcular el precio. */
    var tirada: Tirada? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class TiradorViewHolder(private val binding: ItemTiradorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Tirador) {
            binding.tvNombre.text = item.nombreApellidos

            val categorias = mutableListOf<String>()
            if (item.esLocal) categorias.add("Local")
            if (item.esJunior) categorias.add("Junior")
            if (item.esSenior) categorias.add("Senior")
            if (item.esDama) categorias.add("Dama")

            val categoriaTexto = if (categorias.isNotEmpty()) {
                categorias.joinToString(", ")
            } else {
                "General"
            }

            val precioTexto = tirada?.let { " · Precio: ${"%.2f".format(it.precioPara(item))}€" } ?: ""
            binding.tvInfo.text = "Categoría: $categoriaTexto$precioTexto · Platos rotos: ${item.platosRotos}"

            binding.root.setOnClickListener {
                onClickListener(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TiradorViewHolder {
        val binding = ItemTiradorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TiradorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TiradorViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Tirador>() {
        override fun areItemsTheSame(oldItem: Tirador, newItem: Tirador): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Tirador, newItem: Tirador): Boolean =
            oldItem == newItem
    }
}
