package oscar.platoscore.models

import oscar.platoscore.utils.Fechas

/**
 * Filtros aplicables a un listado de tiradas. Todos son opcionales (null = sin
 * filtrar por ese campo). El rango de fechas se expresa en epoch millis:
 * [desde] = inicio del día, [hasta] = fin del día.
 */
data class FiltroTiradas(
    val tipo: String? = null,
    val maquina: String? = null,
    val desde: Long? = null,
    val hasta: Long? = null
) {
    val activo: Boolean
        get() = tipo != null || maquina != null || desde != null || hasta != null

    /** ¿Pasa una tirada personal el filtro? (tipo, máquina y fechas). */
    fun acepta(t: TiradaPersonalConSeries): Boolean =
        (tipo == null || t.tirada.tipo == tipo) &&
            (maquina == null || t.tirada.maquina == maquina) &&
            (desde == null || t.tirada.fechaHora >= desde) &&
            (hasta == null || t.tirada.fechaHora <= hasta)

    /** ¿Pasa una tirada profesional el filtro? (solo fechas; no tiene tipo ni máquina). */
    fun acepta(t: TiradaConContadores): Boolean {
        val desdeIso = desde?.let { Fechas.isoDeMillis(it) }
        val hastaIso = hasta?.let { Fechas.isoDeMillis(it) }
        return (desdeIso == null || t.tirada.fecha >= desdeIso) &&
            (hastaIso == null || t.tirada.fecha <= hastaIso)
    }
}
