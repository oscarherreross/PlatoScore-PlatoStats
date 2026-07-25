package oscar.platoscore.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import kotlin.math.ceil
import kotlin.math.max

/**
 * Gráfica de líneas mínima y sin dependencias externas. Pinta una o varias
 * series de valores en porcentaje (0..100). El eje Y es fijo de 0 a 100 % con
 * líneas guía en 0/50/100 y el eje X reparte las tiradas de izquierda (más
 * antigua) a derecha (más reciente), etiquetadas por su número de tirada. Los
 * valores nulos dejan hueco en la línea (p. ej. tiradas sin dato de primer tiro).
 */
class LineChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /** Una serie de la gráfica: valores (nullable = hueco) y su color. */
    data class Serie(val valores: List<Float?>, val color: Int)

    private var series: List<Serie> = emptyList()

    private val colorGuia = Color.parseColor("#40808080")
    private val colorTexto = Color.GRAY

    private val pintaLinea = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(2.5f)
    }
    private val pintaPunto = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
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

    fun setSeries(nuevas: List<Serie>) {
        series = nuevas
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val margenIzq = dp(36f)
        val margenAbajo = dp(28f)
        val margenArriba = dp(8f)
        val margenDer = dp(12f)

        val anchoUtil = width - margenIzq - margenDer
        val altoUtil = height - margenArriba - margenAbajo
        if (anchoUtil <= 0 || altoUtil <= 0) return

        fun yDe(porcentaje: Float): Float =
            margenArriba + altoUtil * (1f - porcentaje / 100f)

        // Líneas guía y etiquetas del eje Y (0, 50, 100).
        for (nivel in intArrayOf(0, 50, 100)) {
            val y = yDe(nivel.toFloat())
            canvas.drawLine(margenIzq, y, (width - margenDer), y, pintaGuia)
            canvas.drawText("$nivel", dp(4f), y + sp(4f), pintaTexto)
        }

        val n = series.maxOfOrNull { it.valores.size } ?: 0
        if (n == 0) return

        fun xDe(indice: Int): Float =
            if (n == 1) margenIzq + anchoUtil / 2f
            else margenIzq + anchoUtil * indice / (n - 1)

        for (serie in series) {
            pintaLinea.color = serie.color
            pintaPunto.color = serie.color
            val v = serie.valores
            // Segmentos solo entre puntos consecutivos con dato.
            for (i in 0 until v.size - 1) {
                val a = v[i]
                val b = v[i + 1]
                if (a != null && b != null) {
                    canvas.drawLine(xDe(i), yDe(a), xDe(i + 1), yDe(b), pintaLinea)
                }
            }
            v.forEachIndexed { i, valor ->
                if (valor != null) canvas.drawCircle(xDe(i), yDe(valor), dp(3.5f), pintaPunto)
            }
        }

        // Etiquetas del eje X (número de tirada), espaciadas para no solaparse.
        val baseY = yDe(0f) + sp(15f)
        val anchoEtiqueta = pintaTexto.measureText("00") + dp(8f)
        val maxEtiquetas = max(1, (anchoUtil / anchoEtiqueta).toInt())
        val paso = max(1, ceil(n.toFloat() / maxEtiquetas).toInt())
        for (i in 0 until n) {
            if (i % paso == 0 || i == n - 1) {
                val txt = (i + 1).toString()
                canvas.drawText(txt, xDe(i) - pintaTexto.measureText(txt) / 2f, baseY, pintaTexto)
            }
        }
    }

    private fun dp(valor: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valor, resources.displayMetrics)

    private fun sp(valor: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, valor, resources.displayMetrics)
}
