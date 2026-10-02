package oscar.platoscore.models

import oscar.plato.core.models.FiltroTiradas
import oscar.plato.core.utils.Fechas

/**
 * ¿Pasa una tirada el filtro? Las tiradas de PlatoScore no tienen tipo ni
 * máquina, así que solo cuenta el rango de fechas (comparado en ISO).
 */
fun FiltroTiradas.acepta(t: TiradaConContadores): Boolean {
    val desdeIso = desde?.let { Fechas.isoDeMillis(it) }
    val hastaIso = hasta?.let { Fechas.isoDeMillis(it) }
    return (desdeIso == null || t.tirada.fecha >= desdeIso) &&
        (hastaIso == null || t.tirada.fecha <= hastaIso)
}
