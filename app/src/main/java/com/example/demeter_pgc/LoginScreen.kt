package com.example.demeter_pgc
import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.auth

@Preview
@Composable

fun LoginScreen(
    onClickRegister: () -> Unit = {},
    onSuccessfulLogin: () -> Unit = {},
    onClickForgotPassword: () -> Unit = {}
) {

    // Usamos LocalContext.current para obtener el contexto de manera segura en Compose.
    val context = LocalContext.current

    // ESTADOS
    var inputEmail by remember { mutableStateOf("") }
    var inputPassword by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf("") } //Error:
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }

    Scaffold { paddingValues ->
        Column(modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp)
            ,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Image(
                painter = painterResource(id = R.drawable.demeter),
                contentDescription = "Logo Demeter",
                modifier = Modifier.size(200.dp)
            )


            Spacer(modifier = Modifier.height(16.dp))  // espacio entre imagen y texto

            Text("Sistema inteligente de monitoreo agrícola",color = Color(0xFF0C7211))


            // Campo de texto para el usuario
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = inputEmail,
                onValueChange = {inputEmail = it},
                label = { Text("Email") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email Icon"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
               supportingText = {
                    if (emailError.isNotEmpty()) {
                        Text(
                            text = emailError,
                            color = Color.Red
                        )
                    }
               },keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrect = false,
                    keyboardType = KeyboardType.Email
                )




                )
            // Campo de texto para la contraseña
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = inputPassword,
                onValueChange = {inputPassword = it},
                label = { Text("Password") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Password Icon"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    if (passwordError.isNotEmpty()) {
                        Text(
                            text = passwordError,
                            color = Color.Red
                        )
                    }
                },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrect = false,
                    keyboardType = KeyboardType.Password
                )
            )

            // Botón de inicio de sesión
            Spacer(modifier = Modifier.height(24.dp))

            if (loginError.isNotEmpty()){
                Text(
                    loginError,
                    color = Color.Red,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
            }

            // Texto de error al iniciar secion

            Button(onClick = {

                val isValidEmail: Boolean = validateEmail(inputEmail).first
                val isValidPassword=validatePassword(inputPassword).first
                emailError=validateEmail(inputEmail).second
                passwordError=validatePassword(inputPassword).second
                val auth = Firebase.auth
                val activity = context as Activity

                // Movimos el acceso a Firebase y el cast de Activity aquí para evitar que el Preview
                // intente inicializar Firebase durante la composición, lo cual causaría un error
                // ya que FirebaseApp no está inicializado en el entorno de Preview.
                if(isValidEmail && isValidPassword){
                    auth.signInWithEmailAndPassword(inputEmail, inputPassword)
                        .addOnCompleteListener(activity) { task ->
                            if (task.isSuccessful){
                                // Si todo esta bien vamos a navegar
                                onSuccessfulLogin()
                            }else{
                                // muestra error si el usuario o constraseña son incorrectos
                                loginError = when(task.exception){
                                    is FirebaseAuthInvalidCredentialsException -> "Correo o contraseña incorrecto"
                                    is FirebaseAuthInvalidUserException -> "El correo no existe"
                                    else -> "Error al iniciar sesión intenta de nuevo"
                                }
                            }
                        }



                }else{

                }


            }, modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0C7211),
                    contentColor = Color.White
                )
            ) {
                Text("Iniciar Sesión")

            }
            // Botón de recuperar contraseña
            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onClickForgotPassword) {
                Text("¿Olvidaste tu contraseña?", color = Color(0xFF0C7211))
            }
            // Botón para registrarse
            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onClickRegister) {
                Text("¿No tienes una cuenta? Regístrate",
                    color = Color(0xFF0C7211)
                )

            }
        }

    }
}



            // Aquí van tus campos de texto,
            // botones, etc.
            // Ejemplo:
            // Text(text = "Login Screen")

