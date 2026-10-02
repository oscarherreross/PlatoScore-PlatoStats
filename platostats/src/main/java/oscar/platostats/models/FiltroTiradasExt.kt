package oscar.platostats.models

import oscar.plato.core.models.FiltroTiradas

/** ¿Pasa una tirada el filtro? (tipo, máquina y fechas). */
fun FiltroTiradas.acepta(t: TiradaConSeries): Boolean {
    // Copias locales: las propiedades de un módulo externo (:core) no admiten smart cast.
    val desde = desde
    val hasta = hasta
    return (tipo == null || t.tirada.tipo == tipo) &&
        (maquina == null || t.tirada.maquina == maquina) &&
        (desde == null || t.tirada.fechaHora >= desde) &&
        (hasta == null || t.tirada.fechaHora <= hasta)
}
