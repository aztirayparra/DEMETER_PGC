package com.example.demeter_pgc

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

/**
 * Pantalla principal de la app.
 *
 * Los callbacks (onNavigateTo..., onLogout) permiten que esta pantalla no
 * dependa directamente del NavController: quien la usa (NavigationApp.kt)
 * decide qué hacer cuando el usuario toca cada opción.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCamera: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToCameraManagement: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    // Controla si el menú desplegable del ícono de perfil está abierto o cerrado.
    var isMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                // Nombre de la app a la izquierda.
                title = {
                    Text("DEMETER PGC", fontWeight = FontWeight.Bold, color = Color(0xFF0C7211))
                },
                // Ícono de perfil a la derecha, con menú de "Cerrar sesión".
                actions = {
                    IconButton(onClick = { isMenuExpanded = true }) {
                        Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "Perfil")
                    }
                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Cerrar sesión") },
                            onClick = {
                                isMenuExpanded = false
                                // Cerramos la sesión en Firebase Auth y avisamos
                                // a NavigationApp para que regrese al login.
                                Firebase.auth.signOut()
                                onLogout()
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Botón principal: lleva a la pantalla de la cámara y clasificación.
            Button(
                onClick = onNavigateToCamera,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0C7211),
                    contentColor = Color.White
                )
            ) {
                Text("Detección de macroinvasores", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón secundario: historial de detecciones.
            OutlinedButton(
                onClick = onNavigateToHistory,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Historial de registros")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón secundario: gestión de cámaras.
            OutlinedButton(
                onClick = onNavigateToCameraManagement,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Activar/Desactivar cámaras")
            }
        }
    }
}