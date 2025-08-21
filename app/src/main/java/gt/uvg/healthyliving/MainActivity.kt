package gt.uvg.healthyliving

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import gt.uvg.healthyliving.ui.theme.HealthyLivingTheme

// Modelo simple: nombre de la receta y URL de su imagen
data class Receta(val nombre: String, val url: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HealthyLivingTheme { PantallaPrincipal() } }
    }
}

/**
 * Componente Custom para mostrar cada elemento de la lista (imagen + texto).
 * Al tocar la tarjeta, se elimina (extra del laboratorio).
 */
@Composable
fun RecetaItem(
    receta: Receta,
    onRemove: (Receta) -> Unit,
    modifier: Modifier = Modifier
) {
    val contexto = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onRemove(receta) }, // eliminar al tocar (extra)
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        // === Extra de estilo (mínimo): bordes redondeados + color suave de contenedor ===
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(Modifier.padding(12.dp)) {

            // Imagen remota con indicador de carga, imagen de respaldo y aviso si falla
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(contexto)
                    .data(receta.url)
                    .crossfade(true) // transición suave de imagen
                    .listener(onError = { _, r ->
                        Toast
                            .makeText(
                                contexto,
                                "No se pudo cargar la imagen: ${r.throwable.message}",
                                Toast.LENGTH_SHORT
                            )
                            .show()
                    })
                    .build(),
                contentDescription = receta.nombre,
                loading = { CircularProgressIndicator(modifier = Modifier.size(20.dp)) },
                error = {
                    // Ícono de sistema como imagen de respaldo
                    Image(
                        painter = painterResource(id = android.R.drawable.ic_menu_report_image),
                        contentDescription = null,
                        modifier = Modifier.size(80.dp)
                    )
                },
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(80.dp)
            )

            Spacer(Modifier.width(12.dp))

            Text(
                text = receta.nombre,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun PantallaPrincipal() {
    // Lista que se muestra en pantalla
    val recetas = remember { mutableStateListOf<Receta>() }

    // Campos que escribe la persona usuaria
    var nombre by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }

    // Mensaje breve para informar acciones (agregado/eliminado/error)
    var mensaje by remember { mutableStateOf("") }

    // Botón habilitado solo si hay nombre y la URL comienza con https
    val botonHabilitado = nombre.isNotBlank() && url.trim().startsWith("https://")

    Column(modifier = Modifier.padding(16.dp)) {

        // Campo: nombre
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre de la receta") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(8.dp))

        // Campo: URL de la imagen
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("URL de la imagen (https)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(8.dp))

        // Agregar (con validación para evitar vacíos y duplicados)  ← Extra del laboratorio
        Button(
            onClick = {
                val n = nombre.trim()
                val u = url.trim()
                val existe = recetas.any { it.nombre.equals(n, ignoreCase = true) }
                if (existe) {
                    mensaje = "Esa receta ya existe."
                } else if (n.isEmpty() || u.isEmpty()) {
                    mensaje = "Completa nombre y URL."
                } else {
                    recetas.add(Receta(n, u))
                    nombre = ""
                    url = ""
                    mensaje = "Receta agregada."
                }
            },
            enabled = botonHabilitado
        ) {
            Text("Agregar")
        }

        if (mensaje.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(Modifier.height(16.dp))

        // Lista de recetas (usando el componente Custom RecetaItem)
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(recetas, key = { it.nombre.lowercase() }) { receta ->
                RecetaItem(
                    receta = receta,
                    onRemove = {
                        recetas.remove(it)
                        mensaje = "Eliminada: ${it.nombre}"
                    }
                )
            }
        }
    }
}
