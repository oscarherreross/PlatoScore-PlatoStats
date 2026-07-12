package oscar.platoscore.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import oscar.platoscore.R
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
            val contexto = binding.root.context
            binding.tvNombre.text = item.nombreApellidos

            val categorias = mutableListOf<String>()
            if (item.esLocal) categorias.add(contexto.getString(R.string.categoria_local))
            if (item.esJunior) categorias.add(contexto.getString(R.string.categoria_junior))
            if (item.esSenior) categorias.add(contexto.getString(R.string.categoria_senior))
            if (item.esDama) categorias.add(contexto.getString(R.string.categoria_dama))

            val categoriaTexto = if (categorias.isNotEmpty()) {
                categorias.joinToString(", ")
            } else {
                contexto.getString(R.string.categoria_general)
            }

            val precioTexto = tirada?.let {
                contexto.getString(R.string.segmento_precio, "%.2f".format(it.precioPara(item)))
            } ?: ""
            binding.tvInfo.text = contexto.getString(
                R.string.item_info_tirador, categoriaTexto, precioTexto, item.platosRotos
            )

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
