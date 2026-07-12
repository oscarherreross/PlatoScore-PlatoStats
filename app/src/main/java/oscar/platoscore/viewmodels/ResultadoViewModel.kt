package oscar.platoscore.viewmodels

import androidx.lifecycle.ViewModel
import oscar.platoscore.models.Resultado
import oscar.platoscore.models.Tirador

class ResultadoViewModel : ViewModel() {

    fun generarResultados(tiradores: List<Tirador>): List<Resultado> {
        val resultados = mutableListOf<Resultado>()

        // Separar tiradores por categoría
        val locales = tiradores.filter { it.esLocal }
        val generales = tiradores.filter { !it.esLocal }
        val juniors = tiradores.filter { it.esJunior }
        val seniors = tiradores.filter { it.esSenior }
        val damas = tiradores.filter { it.esDama }

        // Ordenar por platos rotos (descendente)
        val localesOrdenados = locales.sortedByDescending { it.platosRotos }
        val generalesOrdenados = generales.sortedByDescending { it.platosRotos }
        val junioresOrdenados = juniors.sortedByDescending { it.platosRotos }
        val senioresOrdenados = seniors.sortedByDescending { it.platosRotos }
        val damasOrdenadas = damas.sortedByDescending { it.platosRotos }

        // Crear resultados para cada tirador
        for (tirador in tiradores) {
            val resultado = Resultado(
                tirador = tirador,
                clasificacionLocal = if (tirador.esLocal) {
                    localesOrdenados.indexOf(tirador) + 1
                } else null,
                clasificacionGeneral = if (!tirador.esLocal) {
                    generalesOrdenados.indexOf(tirador) + 1
                } else null,
                clasificacionJunior = if (tirador.esJunior) {
                    junioresOrdenados.indexOf(tirador) + 1
                } else null,
                clasificacionSenior = if (tirador.esSenior) {
                    senioresOrdenados.indexOf(tirador) + 1
                } else null,
                clasificacionDama = if (tirador.esDama) {
                    damasOrdenadas.indexOf(tirador) + 1
                } else null,
                totalPuntos = (tirador.platosRotos * tirador.precio)
            )
            resultados.add(resultado)
        }

        return resultados
    }
}
