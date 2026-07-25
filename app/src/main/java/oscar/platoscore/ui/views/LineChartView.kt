package oscar.platoscore.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import oscar.platoscore.R

/**
 * Gráfica de líneas mínima y sin dependencias externas: pinta una serie de
 * valores en porcentaje (0..100) mostrando la evolución del tirador. El eje Y
 * es fijo de 0 a 100 % con líneas guía en 0/50/100 y el eje X reparte las
 * tiradas de izquierda (más antigua) a derecha (más reciente).
 */
class LineChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var valores: List<Float> = emptyList()

    private val colorLinea = resolverColorPrimario()
    private val colorGuia = Color.parseColor("#40808080")
    private val colorTexto = resolverColorTexto()

    private val pintaLinea = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(2.5f)
        color = colorLinea
    }
    private val pintaPunto = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = colorLinea
    }
    private val pintaGuia = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(1f)
        color = colorGuia
    }
    private val pintaTexto = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorTexto
        textSize = sp(12f)
    }

    fun setValores(nuevos: List<Float>) {
        valores = nuevos
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val margenIzq = dp(36f)
        val margenAbajo = dp(20f)
        val margenArriba = dp(8f)
        val margenDer = dp(12f)

        val anchoUtil = width - margenIzq - margenDer
        val altoUtil = height - margenArriba - margenAbajo
        if (anchoUtil <= 0 || altoUtil <= 0) return

        // Función para pasar un porcentaje (0..100) a coordenada Y.
        fun yDe(porcentaje: Float): Float =
            margenArriba + altoUtil * (1f - porcentaje / 100f)

        // Líneas guía y etiquetas del eje Y (0, 50, 100).
        for (nivel in intArrayOf(0, 50, 100)) {
            val y = yDe(nivel.toFloat())
            canvas.drawLine(margenIzq, y, (width - margenDer), y, pintaGuia)
            canvas.drawText("$nivel", dp(4f), y + sp(4f), pintaTexto)
        }

        if (valores.isEmpty()) return

        // Coordenada X de cada valor. Con un solo punto se centra.
        fun xDe(indice: Int): Float =
            if (valores.size == 1) margenIzq + anchoUtil / 2f
            else margenIzq + anchoUtil * indice / (valores.size - 1)

        // Línea que une los puntos.
        for (i in 0 until valores.size - 1) {
            canvas.drawLine(
                xDe(i), yDe(valores[i]),
                xDe(i + 1), yDe(valores[i + 1]),
                pintaLinea
            )
        }
        // Puntos.
        valores.forEachIndexed { i, v ->
            canvas.drawCircle(xDe(i), yDe(v), dp(3.5f), pintaPunto)
        }
    }

    private fun resolverColorPrimario(): Int {
        val tv = TypedValue()
        val encontrado = context.theme.resolveAttribute(
            com.google.android.material.R.attr.colorPrimary, tv, true
        )
        return if (encontrado) tv.data else ContextCompat.getColor(context, R.color.purple_500)
    }

    private fun resolverColorTexto(): Int {
        val tv = TypedValue()
        val encontrado = context.theme.resolveAttribute(
            android.R.attr.textColorSecondary, tv, true
        )
        return if (encontrado) {
            if (tv.resourceId != 0) ContextCompat.getColor(context, tv.resourceId) else tv.data
        } else Color.GRAY
    }

    private fun dp(valor: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valor, resources.displayMetrics)

    private fun sp(valor: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, valor, resources.displayMetrics)
}
